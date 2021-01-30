package net.blueberrymc.network.client.handshake;

import net.blueberrymc.network.mod.ModInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientboundBlueberryHandshakePacket implements Packet<ClientBlueberryHandshakePacketListener> {
    private List<ModInfo> modInfos;

    public ClientboundBlueberryHandshakePacket() {}

    public ClientboundBlueberryHandshakePacket(List<ModInfo> modInfos) {
        this.modInfos = modInfos;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.modInfos = new ArrayList<>();
        int size = friendlyByteBuf.readInt();
        for (int i = 0; i < size; i++) {
            String modId = friendlyByteBuf.readUtf();
            String version = friendlyByteBuf.readUtf();
            modInfos.add(new ModInfo(modId, version));
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        List<ModInfo> modInfoList = modInfos;
        friendlyByteBuf.writeInt(modInfoList.size());
        for (ModInfo modInfo : modInfoList) {
            friendlyByteBuf.writeUtf(modInfo.modId);
            friendlyByteBuf.writeUtf(modInfo.version);
        }
    }

    @Override
    public void handle(ClientBlueberryHandshakePacketListener clientBlueberryPacketListener) {
        clientBlueberryPacketListener.handleBlueberryHandshakeResponse(this);
    }

    /**
     * Returns a list of mods that the server has installed. Used for version comparison, if different version
     * between client were used, the client will be rejected.
     * @return the list of mods
     */
    public List<ModInfo> getModInfos() {
        return modInfos;
    }
}
