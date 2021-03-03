package net.blueberrymc.common;

import net.minecraft.CrashReport;
import net.minecraft.server.MinecraftServer;
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

    @Override
    public void crash(@NotNull CrashReport crashReport) {
        throw new UnsupportedOperationException();
    }
}
