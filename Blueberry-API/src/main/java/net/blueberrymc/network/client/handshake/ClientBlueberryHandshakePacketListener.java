package net.blueberrymc.network.client.handshake;

import net.minecraft.network.PacketListener;

public interface ClientBlueberryHandshakePacketListener extends PacketListener {
    void handleBlueberryHandshakeResponse(ClientboundBlueberryHandshakePacket packet);
}
