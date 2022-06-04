package net.blueberrymc.network;

import net.kyori.adventure.key.Key;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface BlueberryPacket<T extends BlueberryPacketListener> {
    @NotNull
    Key getId();
    void write(@NotNull FriendlyByteBuf buf) throws IOException;
    void handle(@NotNull T packetListener);
}
