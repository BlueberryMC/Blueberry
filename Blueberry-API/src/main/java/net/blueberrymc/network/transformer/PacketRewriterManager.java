package net.blueberrymc.network.transformer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.blueberrymc.network.transformer.rewriters.S21w37a_To_v1_17_1;
import net.blueberrymc.network.transformer.rewriters.S21w38a_To_S21w37a;
import net.blueberrymc.network.transformer.rewriters.S21w40a_To_S21w39a;
import net.blueberrymc.network.transformer.rewriters.S21w41a_To_S21w40a;
import net.blueberrymc.network.transformer.rewriters.S21w42a_To_S21w41a;
import net.blueberrymc.network.transformer.rewriters.S21w43a_To_S21w42a;
import net.blueberrymc.network.transformer.rewriters.S21w44a_To_S21w43a;
import net.blueberrymc.network.transformer.rewriters.v1_17_1_To_v1_17;
import net.blueberrymc.network.transformer.rewriters.v1_18_Pre5_To_v1_18_Pre4;
import net.minecraft.SharedConstants;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

// this is not implemented for the server
public class PacketRewriterManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<PacketRewriter> REWRITER_LIST = new ArrayList<>();

    static {
        register();
    }

    public static void register() {
        REWRITER_LIST.clear();
        // list order: older versions -> newer versions
        REWRITER_LIST.add(new v1_17_1_To_v1_17());
        REWRITER_LIST.add(new S21w37a_To_v1_17_1());
        REWRITER_LIST.add(new S21w38a_To_S21w37a());
        REWRITER_LIST.add(PacketRewriter.of(TransformableProtocolVersions.SNAPSHOT_21W39A, TransformableProtocolVersions.SNAPSHOT_21W38A));
        REWRITER_LIST.add(new S21w40a_To_S21w39a());
        REWRITER_LIST.add(new S21w41a_To_S21w40a());
        REWRITER_LIST.add(new S21w42a_To_S21w41a());
        REWRITER_LIST.add(new S21w43a_To_S21w42a());
        REWRITER_LIST.add(new S21w44a_To_S21w43a());
        REWRITER_LIST.add(PacketRewriter.of(TransformableProtocolVersions.v1_18_PRE1, TransformableProtocolVersions.SNAPSHOT_21W44A));
        REWRITER_LIST.add(PacketRewriter.of(TransformableProtocolVersions.v1_18_PRE2, TransformableProtocolVersions.v1_18_PRE1));
        REWRITER_LIST.add(PacketRewriter.of(TransformableProtocolVersions.v1_18_PRE3, TransformableProtocolVersions.v1_18_PRE2));
        REWRITER_LIST.add(PacketRewriter.of(TransformableProtocolVersions.v1_18_PRE4, TransformableProtocolVersions.v1_18_PRE3));
        REWRITER_LIST.add(new v1_18_Pre5_To_v1_18_Pre4());
        var list = new ArrayList<>(REWRITER_LIST);
        Collections.reverse(list);
        list.forEach(PacketRewriter::register);
    }

    @NotNull
    public static List<PacketRewriter> collectRewriters(int targetPV, boolean flip) throws NoSuchElementException {
        return collectRewriters(REWRITER_LIST.get(REWRITER_LIST.size() - 1).getSourcePV(), targetPV, flip);
    }

    @NotNull
    public static List<PacketRewriter> collectRewriters(int sourcePV, int targetPV, boolean flip) throws NoSuchElementException {
        PacketRewriter source = REWRITER_LIST.stream()
                .filter(rewriter -> rewriter.getSourcePV() == sourcePV)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No packet rewriter set for " + sourcePV + " (source)"));
        if (source.getSourcePV() == targetPV) {
            return Collections.emptyList();
        }
        if (source.getTargetPV() == targetPV) {
            return Collections.singletonList(REWRITER_LIST.get(REWRITER_LIST.size() - 1));
        }
        PacketRewriter entry = REWRITER_LIST.stream()
                .filter(rewriter -> rewriter.getTargetPV() == targetPV)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No packet rewriter set for " + targetPV + " (target)"));
        int index = REWRITER_LIST.indexOf(entry);
        List<PacketRewriter> rewriterList = new ArrayList<>();
        for (int i = index; i <= REWRITER_LIST.indexOf(source); i++) {
            rewriterList.add(REWRITER_LIST.get(i));
        }
        if (flip) Collections.reverse(rewriterList);
        return rewriterList;
    }

    @NotNull
    public static ByteBuf rewriteInbound(@NotNull ConnectionProtocol protocol, @NotNull ByteBuf byteBuf, int targetPV) {
        FriendlyByteBuf read = new FriendlyByteBuf(Unpooled.buffer());
        read.writeBytes(byteBuf, byteBuf.readerIndex(), byteBuf.readableBytes());
        int packetId = read.readVarInt();
        int readerIndex = read.readerIndex();
        read.resetReaderIndex();
        int currentPV = targetPV;
        FriendlyByteBuf write = new FriendlyByteBuf(Unpooled.buffer());
        for (PacketRewriter rewriter : collectRewriters(targetPV, false)) {
            if (currentPV != rewriter.getTargetPV()) {
                throw new IllegalStateException("currentPV (" + currentPV + ") != targetPV (" + rewriter.getTargetPV() + ")");
            }
            packetId = rewriter.getInboundId(protocol, packetId);
            read.readerIndex(readerIndex);
            write.writeVarInt(packetId);
            try {
                rewriter.doRewriteInbound(protocol, packetId, new PacketWrapper(read, write));
            } catch (Exception e) {
                LOGGER.error("Failed to rewrite inbound packet in " + rewriter.getClass().getTypeName(), e);
                throw e;
            }
            read.release();
            read = write;
            write = new FriendlyByteBuf(Unpooled.buffer());
            // set currentPV to source (newer) PV of current rewriter
            currentPV = rewriter.getSourcePV();
        }
        write.release();
        if (currentPV != SharedConstants.getProtocolVersion()) {
            throw new IllegalStateException("currentPV (" + currentPV + ") != client PV (" + SharedConstants.getProtocolVersion() + ")");
        }
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
        for (PacketRewriter rewriter : collectRewriters(targetPV, true)) {
            if (currentPV != rewriter.getSourcePV()) {
                throw new IllegalStateException("currentPV (" + currentPV + ") != sourcePV (" + rewriter.getSourcePV() + ")");
            }
            packetId = rewriter.getOutboundId(protocol, packetId);
            read.readerIndex(readerIndex);
            write.writeVarInt(packetId);
            try {
                rewriter.doRewriteOutbound(protocol, packetId, new PacketWrapper(read, write));
            } catch (Exception e) {
                LOGGER.error("Failed to rewrite outbound packet in " + rewriter.getClass().getTypeName(), e);
                throw e;
            }
            read.release();
            read = write;
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
        for (PacketRewriter rewriter : collectRewriters(sourcePV, targetPV, false)) {
            packetId = rewriter.getInboundId(protocol, packetId);
        }
        return packetId;
    }

    public static int remapInboundPacketId(@NotNull ConnectionProtocol protocol, int packetId, int targetPV) {
        for (PacketRewriter rewriter : collectRewriters(targetPV, false)) {
            packetId = rewriter.getInboundId(protocol, packetId);
        }
        return packetId;
    }

    public static int remapOutboundPacketId(@NotNull ConnectionProtocol protocol, int packetId, int sourcePV, int targetPV) {
        for (PacketRewriter rewriter : collectRewriters(sourcePV, targetPV, true)) {
            packetId = rewriter.getOutboundId(protocol, packetId);
        }
        return packetId;
    }

    public static int remapOutboundPacketId(@NotNull ConnectionProtocol protocol, int packetId, int targetPV) {
        for (PacketRewriter rewriter : collectRewriters(targetPV, true)) {
            packetId = rewriter.getOutboundId(protocol, packetId);
        }
        return packetId;
    }
}
