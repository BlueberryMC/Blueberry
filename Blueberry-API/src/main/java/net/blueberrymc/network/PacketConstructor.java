package net.blueberrymc.network;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PacketConstructor<T extends BlueberryPacketListener> {
    @NotNull
    BlueberryPacket<T> create(@NotNull FriendlyByteBuf buf);
}
