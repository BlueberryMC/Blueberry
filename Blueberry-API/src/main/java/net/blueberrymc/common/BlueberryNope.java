package net.blueberrymc.common;

import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;
import net.blueberrymc.common.scheduler.NoopBlueberryScheduler;
import net.minecraft.CrashReport;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BlueberryNope implements BlueberryUtil {
    @Override
    public @NotNull ResourceManager getResourceManager() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public CompletableFuture<Void> reloadResourcePacks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void crash(@NotNull CrashReport crashReport) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull AbstractBlueberryScheduler getClientScheduler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull AbstractBlueberryScheduler getServerScheduler() {
        return NoopBlueberryScheduler.INSTANCE;
    }
}
