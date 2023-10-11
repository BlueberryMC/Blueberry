package net.blueberrymc.network;

import net.minecraft.resources.ResourceLocation;

public interface BlueberryCustomPayload {
    ResourceLocation id();
    byte[] payload();
}
