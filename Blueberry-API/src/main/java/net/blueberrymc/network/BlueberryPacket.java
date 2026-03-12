package net.blueberrymc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface BlueberryPacket<T extends BlueberryPacketListener> {
    @NotNull
    Identifier getId();
    void write(@NotNull FriendlyByteBuf buf) throws IOException;
    void handle(@NotNull T packetListener);
}
