package net.blueberrymc.common.network;

import net.minecraft.network.PacketListener;

public interface ClientBlueberryPacketListener extends PacketListener {
    void handleBlueberryHandshakeResponse(ClientboundBlueberryHandshakePacket packet);
}
