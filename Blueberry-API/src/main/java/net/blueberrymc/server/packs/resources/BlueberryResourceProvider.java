package net.blueberrymc.server.packs.resources;

import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.NotNull;

public interface BlueberryResourceProvider {
    default boolean remove(@NotNull PackResources packResources) {
        return false;
    }
}
