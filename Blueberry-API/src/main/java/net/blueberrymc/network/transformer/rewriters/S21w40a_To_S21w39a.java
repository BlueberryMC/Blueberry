package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.PacketWrapper;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import net.minecraft.network.ConnectionProtocol;
import org.jetbrains.annotations.NotNull;

public class S21w40a_To_S21w39a extends S21w42a_To_S21w41a {
    public S21w40a_To_S21w39a() {
        this(TransformableProtocolVersions.SNAPSHOT_21W40A, TransformableProtocolVersions.SNAPSHOT_21W39A);
    }

    protected S21w40a_To_S21w39a(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    protected void preRegister() {
    }

    @Override
    public void registerOutbound() {
    }

    @Override
    public void registerInbound() {
        remapInbound(ConnectionProtocol.PLAY, 0x57, 0x58);
        remapInbound(ConnectionProtocol.PLAY, 0x58, 0x59);
        remapInbound(ConnectionProtocol.PLAY, 0x59, 0x5A);
        remapInbound(ConnectionProtocol.PLAY, 0x5A, 0x5B);
        remapInbound(ConnectionProtocol.PLAY, 0x5B, 0x5C);
        remapInbound(ConnectionProtocol.PLAY, 0x5C, 0x5D);
        remapInbound(ConnectionProtocol.PLAY, 0x5D, 0x5E);
        remapInbound(ConnectionProtocol.PLAY, 0x5E, 0x5F);
        remapInbound(ConnectionProtocol.PLAY, 0x5F, 0x60);
        remapInbound(ConnectionProtocol.PLAY, 0x60, 0x61);
        remapInbound(ConnectionProtocol.PLAY, 0x61, 0x62);
        remapInbound(ConnectionProtocol.PLAY, 0x62, 0x63);
        remapInbound(ConnectionProtocol.PLAY, 0x63, 0x64);
        remapInbound(ConnectionProtocol.PLAY, 0x64, 0x65);
        remapInbound(ConnectionProtocol.PLAY, 0x65, 0x66);
        remapInbound(ConnectionProtocol.PLAY, 0x66, 0x67);
        rewriteInbound(ConnectionProtocol.PLAY, 0x26, wrapper -> {
            wrapper.passthrough(PacketWrapper.Type.INT); // Entity ID
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Is hardcore
            wrapper.passthrough(PacketWrapper.Type.BYTE); // Game mode
            wrapper.passthrough(PacketWrapper.Type.BYTE); // Previous game mode
            wrapper.passthroughCollection(PacketWrapper.Type.RESOURCE_LOCATION); // World count / World names
            wrapper.passthrough(PacketWrapper.Type.NBT); // Dimension Codec
            wrapper.passthrough(PacketWrapper.Type.NBT); // Dimension
            wrapper.passthrough(PacketWrapper.Type.RESOURCE_LOCATION); // World name
            wrapper.passthrough(PacketWrapper.Type.LONG); // Hashed seed
            wrapper.passthrough(PacketWrapper.Type.VAR_INT); // Max players
            wrapper.passthrough(PacketWrapper.Type.VAR_INT); // View distance
            wrapper.writeVarInt(8); // Simulation distance
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Reduced debug info
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Enable respawn screen
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Is debug
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // Is flat
        });
    }

    @Override
    protected void registerSoundRewriter() {
        registerSoundRewriter(0x5B, 0x5C);
    }

    @Override
    protected void registerItemRewriter() {
        registerItemRewriter(0x08, 0x28, 0x14, 0x16, 0x28, 0x4D, 0x50, 0x62, 0x65);
    }
}
