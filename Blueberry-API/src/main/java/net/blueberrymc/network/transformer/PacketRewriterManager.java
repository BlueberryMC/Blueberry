package net.blueberrymc.network.transformer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.blueberrymc.network.transformer.rewriters.S21w38a_To_S21w37a;
import net.blueberrymc.network.transformer.rewriters.S21w39a_To_S21w38a;
import net.blueberrymc.network.transformer.rewriters.S21w40a_To_S21w39a;
import net.blueberrymc.network.transformer.rewriters.S21w41a_To_S21w40a;
import net.blueberrymc.network.transformer.rewriters.S21w42a_To_S21w41a;
import net.blueberrymc.network.transformer.rewriters.S21w43a_To_S21w42a;
import net.blueberrymc.network.transformer.rewriters.S21w44a_To_S21w43a;
import net.blueberrymc.network.transformer.rewriters.V1_18_Pre1_To_S21w44a;
import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

// this is not implemented for the server
public class PacketRewriterManager {
    private static final List<PacketRewriter> REWRITER_LIST = new ArrayList<>();

    static {
        register();
    }

    public static void register() {
        REWRITER_LIST.clear();
        // list order: older versions -> newer versions
        REWRITER_LIST.add(new S21w38a_To_S21w37a());
        REWRITER_LIST.add(new S21w39a_To_S21w38a());
        REWRITER_LIST.add(new S21w40a_To_S21w39a());
        REWRITER_LIST.add(new S21w41a_To_S21w40a());
        REWRITER_LIST.add(new S21w42a_To_S21w41a());
        REWRITER_LIST.add(new S21w43a_To_S21w42a());
        REWRITER_LIST.add(new S21w44a_To_S21w43a());
        REWRITER_LIST.add(new V1_18_Pre1_To_S21w44a());
        REWRITER_LIST.forEach(PacketRewriter::register);
    }

    @NotNull
    public static List<PacketRewriter> collectRewriters(int targetPV) throws NoSuchElementException {
        return collectRewriters(REWRITER_LIST.get(REWRITER_LIST.size() - 1).getSourcePV(), targetPV);
    }

    @NotNull
    public static List<PacketRewriter> collectRewriters(int sourcePV, int targetPV) throws NoSuchElementException {
        PacketRewriter source = REWRITER_LIST.stream()
                .filter(rewriter -> rewriter.getSourcePV() == sourcePV)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        if (source.getSourcePV() == targetPV) {
            return Collections.emptyList();
        }
        if (source.getTargetPV() == targetPV) {
            return Collections.singletonList(REWRITER_LIST.get(REWRITER_LIST.size() - 1));
        }
        PacketRewriter entry = REWRITER_LIST.stream()
                .filter(rewriter -> rewriter.getTargetPV() == targetPV)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        int index = REWRITER_LIST.indexOf(entry);
        List<PacketRewriter> rewriterList = new ArrayList<>();
        for (int i = index; i <= REWRITER_LIST.indexOf(source); i++) {
            rewriterList.add(REWRITER_LIST.get(i));
        }
        Collections.reverse(rewriterList);
        return rewriterList;
    }

    @NotNull
    public static ByteBuf rewriteInbound(@NotNull ConnectionProtocol protocol, @NotNull ByteBuf byteBuf, int targetPV) {
        FriendlyByteBuf read = new FriendlyByteBuf(Unpooled.buffer());
        read.writeBytes(byteBuf, byteBuf.readerIndex(), byteBuf.readableBytes());
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
            // reset read
            //read.clear();
            read.release();
            // swap read and write
            FriendlyByteBuf originalRead = read;
            read = write;
            //write = originalRead;
            write = new FriendlyByteBuf(Unpooled.buffer());
            // set currentPV to target PV of current rewriter
            currentPV = rewriter.getTargetPV();
        }
        write.release();
        if (currentPV != targetPV) throw new IllegalStateException("currentPV (" + currentPV + ") != targetPV (" + targetPV + ")");
        read.resetReaderIndex();
        return read;
    }

    @NotNull
    public static ByteBuf rewriteOutbound(@NotNull ConnectionProtocol protocol, @NotNull ByteBuf byteBuf, int targetPV) {
        FriendlyByteBuf read = new FriendlyByteBuf(Unpooled.buffer());
        read.writeBytes(byteBuf, byteBuf.readerIndex(), byteBuf.readableBytes());
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
            //read.clear();
            read.release();
            // swap read and write
            FriendlyByteBuf originalRead = read;
            read = write;
            //write = originalRead;
            write = new FriendlyByteBuf(Unpooled.buffer());
            // set currentPV to target PV of current rewriter
            currentPV = rewriter.getTargetPV();
        }
        write.release();
        if (currentPV != targetPV) throw new IllegalStateException("currentPV (" + currentPV + ") != targetPV (" + targetPV + ")");
        read.resetReaderIndex();
        return read;
    }

    public static int remapInboundPacketId(@NotNull ConnectionProtocol protocol, int packetId, int sourcePV, int targetPV) {
        for (PacketRewriter rewriter : collectRewriters(sourcePV, targetPV)) {
            packetId = rewriter.getInboundId(protocol, packetId);
        }
        return packetId;
    }

    public static int remapInboundPacketId(@NotNull ConnectionProtocol protocol, int packetId, int targetPV) {
        for (PacketRewriter rewriter : collectRewriters(targetPV)) {
            packetId = rewriter.getInboundId(protocol, packetId);
        }
        return packetId;
    }

    public static int remapOutboundPacketId(@NotNull ConnectionProtocol protocol, int packetId, int sourcePV, int targetPV) {
        for (PacketRewriter rewriter : collectRewriters(sourcePV, targetPV)) {
            packetId = rewriter.getOutboundId(protocol, packetId);
        }
        return packetId;
    }

    public static int remapOutboundPacketId(@NotNull ConnectionProtocol protocol, int packetId, int targetPV) {
        for (PacketRewriter rewriter : collectRewriters(targetPV)) {
            packetId = rewriter.getOutboundId(protocol, packetId);
        }
        return packetId;
    }
}
