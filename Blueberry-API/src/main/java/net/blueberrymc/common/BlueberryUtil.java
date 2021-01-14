package net.blueberrymc.common;

import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlueberryUtil {
    /**
     * Get a resource manager. Returns null for server.
     * @return resource manager
     */
    @Nullable
    ResourceManager getResourceManager();
}
