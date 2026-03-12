package net.blueberrymc.network;

import net.minecraft.resources.Identifier;

public interface BlueberryCustomPayload {
    Identifier id();
    byte[] payload();
}
