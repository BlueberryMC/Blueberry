package net.blueberrymc.server.packs.resources;

import net.minecraft.server.packs.PackResources;

public interface BlueberryResourceProvider {
    default boolean remove(PackResources packResources) {
        return false;
    }
}
