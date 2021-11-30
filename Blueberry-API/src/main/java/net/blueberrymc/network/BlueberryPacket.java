package net.blueberrymc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface BlueberryPacket<T extends BlueberryPacketListener> {
    @NotNull
    ResourceLocation getId();
    void write(@NotNull FriendlyByteBuf buf) throws IOException;
    void handle(@NotNull T packetListener);
}
