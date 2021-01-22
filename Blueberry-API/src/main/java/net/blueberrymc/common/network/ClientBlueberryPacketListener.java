package net.blueberrymc.common.network;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

public interface ClientBlueberryPacketListener extends PacketListener {
    void handleBlueberryHandshakeResponse(ClientboundBlueberryHandshakePacket packet);
    default void handleCustomPayload(ClientboundCustomPayloadPacket packet) {}
}
