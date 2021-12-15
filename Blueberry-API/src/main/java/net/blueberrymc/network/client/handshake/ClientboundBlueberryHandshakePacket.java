package net.blueberrymc.network.client.handshake;

import net.blueberrymc.network.mod.ModInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClientboundBlueberryHandshakePacket implements Packet<ClientStatusPacketListener> {
    private final List<ModInfo> modInfos;

    public ClientboundBlueberryHandshakePacket(@NotNull List<ModInfo> modInfos) {
        this.modInfos = modInfos;
    }

    public ClientboundBlueberryHandshakePacket(@NotNull FriendlyByteBuf friendlyByteBuf) {
        this.modInfos = new ArrayList<>();
        int size = friendlyByteBuf.readInt();
        for (int i = 0; i < size; i++) {
            String modId = friendlyByteBuf.readUtf();
            String version = friendlyByteBuf.readUtf();
            modInfos.add(new ModInfo(modId, version));
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeCollection(modInfos, (buf, modInfo) -> {
            buf.writeUtf(modInfo.modId);
            buf.writeUtf(modInfo.version);
        });
    }

    @Override
    public void handle(@NotNull ClientStatusPacketListener clientBlueberryPacketListener) {
        ((ClientBlueberryHandshakePacketListener) clientBlueberryPacketListener).handleBlueberryHandshakeResponse(this);
    }

    /**
     * Returns a list of mods that the server has installed. Used for version comparison, if different version
     * between client were used, the client will be rejected.
     * @return the list of mods
     */
    @NotNull
    public List<ModInfo> getModInfos() {
        return modInfos;
    }
}
