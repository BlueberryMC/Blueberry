package net.blueberrymc.server;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;
import net.blueberrymc.server.scheduler.BlueberryServerScheduler;
import net.minecraft.CrashReport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class BlueberryServer extends BlueberryUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlueberryServerScheduler serverScheduler = new BlueberryServerScheduler();
    @Nullable private final BlueberryServer impl;

    public BlueberryServer() {
        this(null);
    }

    public BlueberryServer(@Nullable BlueberryServer impl) {
        this.impl = impl;
    }

    @Override
    public @NotNull ResourceManager getResourceManager() {
        return this.server.getResourceManager();
    }

    private MinecraftServer server = null;
    public void setServer(@NotNull MinecraftServer minecraftServer) {
        if (this.server != null) throw new IllegalArgumentException("Cannot redefine MinecraftServer");
        this.server = minecraftServer;
    }

    /**
     * Returns the current minecraft server. May be null if not initialized yet.
     * @return minecraft server
     */
    @Nullable
    public MinecraftServer getServer() {
        return server;
    }

    public void stopServer() {
        this.server.stopServer();
    }

    @NotNull
    @Override
    public CompletableFuture<Void> reloadResourcePacks() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void crash(@NotNull CrashReport crashReport) {
        Preconditions.checkNotNull(crashReport, "crashReport cannot be null");
        File file = new File(new File("crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
        LOGGER.error(crashReport.getFriendlyReport());
        if (crashReport.saveToFile(file)) {
            LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
        } else {
            LOGGER.error("We were unable to save this crash report to disk.");
        }
        this.stopServer();
        System.exit(1);
    }

    @Override
    public @NotNull AbstractBlueberryScheduler getClientScheduler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull AbstractBlueberryScheduler getServerScheduler() {
        return serverScheduler;
    }

    @Override
    public boolean isOnGameThread() {
        return (server != null && server.isSameThread()) || Thread.currentThread().getName().equals("main");
    }

    @NotNull
    @Override
    public BlueberryServer getImpl() {
        if (impl == null) throw new IllegalArgumentException("impl isn't defined (yet)");
        return impl;
    }
}
