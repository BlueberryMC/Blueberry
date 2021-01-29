package net.blueberrymc.common;

import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public class BlueberryNope implements BlueberryUtil {
    @Override
    public @NotNull ResourceManager getResourceManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reloadResourcePacks() {
        throw new UnsupportedOperationException();
    }
}
