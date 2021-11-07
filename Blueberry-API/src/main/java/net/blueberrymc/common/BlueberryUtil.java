package net.blueberrymc.common;

import net.arikia.dev.drpc.DiscordRichPresence;
import net.blueberrymc.client.BlueberryClient;
import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;
import net.blueberrymc.common.util.ActionableResult;
import net.blueberrymc.common.util.BlueberryEvil;
import net.blueberrymc.common.util.SimpleEntry;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.server.BlueberryServer;
import net.minecraft.CrashReport;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public abstract class BlueberryUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Get a resource manager. Returns null for server.
     * @return resource manager
     */
    @Nullable
    public abstract ResourceManager getResourceManager();

    /**
     * Reloads the client resource packs. This method returns CompletedFuture of value null.
     * @return completable future
     */
    @NotNull
    public abstract CompletableFuture<Void> reloadResourcePacks();

    /**
     * Crashes the Minecraft using provided crash report. Destroys window if on client, and stops the server if on server.
     */
    public abstract void crash(@NotNull CrashReport crashReport);

    /**
     * Gets task scheduler for client.
     * @return scheduler
     * @throws UnsupportedOperationException Thrown when the side is not a client
     */
    @NotNull
    public abstract AbstractBlueberryScheduler getClientScheduler();

    /**
     * Gets task scheduler for server. This method should be available from both sides.
     * @return scheduler
     */
    @NotNull
    public abstract AbstractBlueberryScheduler getServerScheduler();

    /**
     * Gets task scheduler for client, but returns nullable value.
     * @return (nullable) scheduler
     */
    @Nullable
    public AbstractBlueberryScheduler getClientSchedulerNullable() {
        try {
            return getClientScheduler();
        } catch (UnsupportedOperationException ex) {
            return null;
        }
    }

    @NotNull
    public ActionableResult<AbstractBlueberryScheduler> getClientSchedulerOptional() {
        return ActionableResult.ofThrowable(this::getClientScheduler);
    }

    public boolean isOnGameThread() {
        return false;
    }

    public static final SimpleEntry<String, String> BLUEBERRY_ICON = SimpleEntry.of("blueberry", Versioning.getVersion().getFullyQualifiedVersion());

    public void updateDiscordStatus(@Nullable String details) {
        updateDiscordStatus(details, null);
    }

    public void updateDiscordStatus(@Nullable String details, @Nullable String state) {
        updateDiscordStatus(details, state, BLUEBERRY_ICON);
    }

    public void updateDiscordStatus(@Nullable String details, @Nullable String state, @Nullable SimpleEntry<String, String> bigImage) {
        updateDiscordStatus(details, state, bigImage, null);
    }

    public void updateDiscordStatus(@Nullable String details, @Nullable String state, @Nullable SimpleEntry<String, String> bigImage, @Nullable SimpleEntry<String, String> smallImage) {
        updateDiscordStatus(details, state, bigImage, smallImage, 0);
    }

    public void updateDiscordStatus(@Nullable String details, @Nullable String state, @Nullable SimpleEntry<String, String> bigImage, @Nullable SimpleEntry<String, String> smallImage, long start) {}

    @Nullable
    public DiscordRichPresence getDiscordRichPresenceQueue() { return null; }

    public void setDiscordRichPresenceQueue(@Nullable DiscordRichPresence discordRichPresence) {}

    public byte@NotNull[] processClass(@NotNull String path, byte @NotNull [] b) {
        try {
            b = BlueberryEvil.convert(b);
        } catch (Throwable ex) {
            LOGGER.error("Could not convert {}", path, ex);
        }
        return b;
    }

    @NotNull
    public BlueberryUtil getImpl() {
        throw new UnsupportedOperationException();
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    public BlueberryClient asClient() {
        return (BlueberryClient) this;
    }

    @NotNull
    public BlueberryServer asServer() {
        return (BlueberryServer) this;
    }
}
