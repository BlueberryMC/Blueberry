package net.blueberrymc.network.client.handshake;

import net.minecraft.network.PacketListener;
import org.jetbrains.annotations.NotNull;

public interface ClientBlueberryHandshakePacketListener extends PacketListener {
    void handleBlueberryHandshakeResponse(@NotNull ClientboundBlueberryHandshakePacket packet);
}
