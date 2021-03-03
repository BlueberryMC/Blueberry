package net.blueberrymc.common;

import net.arikia.dev.drpc.DiscordRichPresence;
import net.blueberrymc.client.BlueberryClient;
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

    default boolean isOnGameThread() {
        return false;
    }

    SimpleEntry<String, String> BLUEBERRY_ICON = SimpleEntry.of("blueberry",Versioning.getVersion().getFullyQualifiedVersion());

    default void updateDiscordStatus(@Nullable String details) {
        updateDiscordStatus(details, null);
    }

    default void updateDiscordStatus(@Nullable String details, @Nullable String state) {
        updateDiscordStatus(details, state, BLUEBERRY_ICON);
    }

    default void updateDiscordStatus(@Nullable String details, @Nullable String state, @Nullable SimpleEntry<String, String> bigImage) {
        updateDiscordStatus(details, state, bigImage, null);
    }

    default void updateDiscordStatus(@Nullable String details, @Nullable String state, @Nullable SimpleEntry<String, String> bigImage, @Nullable SimpleEntry<String, String> smallImage) {
        updateDiscordStatus(details, state, bigImage, smallImage, 0);
    }

    default void updateDiscordStatus(@Nullable String details, @Nullable String state, @Nullable SimpleEntry<String, String> bigImage, @Nullable SimpleEntry<String, String> smallImage, long start) {}

    @Nullable
    default DiscordRichPresence getDiscordRichPresenceQueue() { return null; }

    default void setDiscordRichPresenceQueue(@Nullable DiscordRichPresence discordRichPresence) {}

    default byte[] processClass(@NotNull String path, byte[] b) {
        try {
            b = BlueberryEvil.convert(b);
        } catch (Throwable ex) {
            LOGGER.error("Could not convert " + path);
        }
        return b;
    }

    @NotNull
    default BlueberryUtil getImpl() {
        throw new UnsupportedOperationException();
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    default BlueberryClient asClient() {
        return (BlueberryClient) this;
    }

    @NotNull
    default BlueberryServer asServer() {
        return (BlueberryServer) this;
    }
}
