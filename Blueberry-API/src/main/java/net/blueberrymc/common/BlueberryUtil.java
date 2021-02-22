package net.blueberrymc.common;

import net.blueberrymc.common.util.BlueberryEvil;
import net.minecraft.CrashReport;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlueberryUtil {
    Logger LOGGER = LogManager.getLogger();

    /**
     * Get a resource manager. Returns null for server.
     * @return resource manager
     */
    @Nullable
    ResourceManager getResourceManager();

    void reloadResourcePacks();

    void crash(@NotNull CrashReport crashReport);

    default byte[] processClass(String path, byte[] b) {
        try {
            b = BlueberryEvil.convert(b);
        } catch (Throwable ex) {
            LOGGER.error("Could not convert " + path);
        }
        return b;
    }
}
