package net.blueberrymc.network.transformer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.blueberrymc.network.transformer.rewriters.S21w44a_To_S21w43a;
import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class PacketRewriterManager {
    private static final List<PacketRewriter> REWRITER_LIST = new ArrayList<>();

    static {
        register();
    }

    public static void register() {
        REWRITER_LIST.clear();
        // list order: older versions -> newer versions
        REWRITER_LIST.add(new S21w44a_To_S21w43a());
    }

    @NotNull
    public static List<PacketRewriter> collectRewriters(int targetPV) throws NoSuchElementException {
        if (REWRITER_LIST.get(REWRITER_LIST.size() - 1).getSourcePV() == targetPV) {
            return Collections.emptyList();
        }
        if (REWRITER_LIST.get(REWRITER_LIST.size() - 1).getTargetPV() == targetPV) {
            return Collections.singletonList(REWRITER_LIST.get(REWRITER_LIST.size() - 1));
        }
        PacketRewriter entry = REWRITER_LIST.stream()
                .filter(rewriter -> rewriter.getTargetPV() == targetPV)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        int index = REWRITER_LIST.indexOf(entry);
        List<PacketRewriter> rewriterList = new ArrayList<>();
        for (int i = index; i < REWRITER_LIST.size(); i++) {
            rewriterList.add(REWRITER_LIST.get(i));
        }
        Collections.reverse(rewriterList);
        return rewriterList;
    }

    @NotNull
    public static ByteBuf rewriteInbound(@NotNull ConnectionProtocol protocol, @NotNull ByteBuf byteBuf, int targetPV) {
        FriendlyByteBuf read = new FriendlyByteBuf(byteBuf);
        int packetId = read.readVarInt();
        int readerIndex = read.readerIndex();
        read.resetReaderIndex();
        int currentPV = SharedConstants.getProtocolVersion();
        FriendlyByteBuf write = new FriendlyByteBuf(Unpooled.buffer());
        for (PacketRewriter rewriter : collectRewriters(targetPV)) {
            if (currentPV != rewriter.getSourcePV()) {
                throw new IllegalStateException("currentPV (" + currentPV + ") != sourcePV (" + rewriter.getSourcePV() + ")");
            }
            packetId = rewriter.getInboundId(protocol, packetId);
            read.readerIndex(readerIndex);
            write.writeVarInt(packetId);
            rewriter.doRewriteInbound(protocol, packetId, new PacketWrapper(read, write));
            read.clear();
            FriendlyByteBuf originalRead = read;
            read = write;
            write = originalRead;
            currentPV = rewriter.getTargetPV();
        }
        if (currentPV != targetPV) throw new IllegalStateException("currentPV (" + currentPV + ") != targetPV (" + targetPV + ")");
        return read;
    }

    @NotNull
    public static ByteBuf rewriteOutbound(@NotNull ConnectionProtocol protocol, @NotNull ByteBuf byteBuf, int targetPV) {
        FriendlyByteBuf read = new FriendlyByteBuf(byteBuf);
        int packetId = read.readVarInt();
        int readerIndex = read.readerIndex();
        read.resetReaderIndex();
        int currentPV = SharedConstants.getProtocolVersion();
        FriendlyByteBuf write = new FriendlyByteBuf(Unpooled.buffer());
        for (PacketRewriter rewriter : collectRewriters(targetPV)) {
            if (currentPV != rewriter.getSourcePV()) {
                throw new IllegalStateException("currentPV (" + currentPV + ") != sourcePV (" + rewriter.getSourcePV() + ")");
            }
            packetId = rewriter.getOutboundId(protocol, packetId);
            read.readerIndex(readerIndex);
            write.writeVarInt(packetId);
            rewriter.doRewriteOutbound(protocol, packetId, new PacketWrapper(read, write));
            // reset read
            read.clear();
            // swap read and write
            FriendlyByteBuf originalRead = read;
            read = write;
            write = originalRead;
            // set currentPV to target PV of current rewriter
            currentPV = rewriter.getTargetPV();
        }
        if (currentPV != targetPV) throw new IllegalStateException("currentPV (" + currentPV + ") != targetPV (" + targetPV + ")");
        return read;
    }
}
