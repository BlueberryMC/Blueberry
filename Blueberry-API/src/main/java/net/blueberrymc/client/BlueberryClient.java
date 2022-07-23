package net.blueberrymc.client;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import de.jcm.discordgamesdk.activity.Activity;
import net.blueberrymc.client.gui.screens.MultiLineBackupConfirmScreen;
import net.blueberrymc.client.renderer.blockentity.MinecraftBlockEntityRenderDispatcher;
import net.blueberrymc.client.scheduler.BlueberryClientScheduler;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.bml.VersionedModInfo;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;
import net.blueberrymc.common.util.InstalledModsContainer;
import net.blueberrymc.common.util.ListUtils;
import net.blueberrymc.common.util.SimpleEntry;
import net.blueberrymc.server.scheduler.BlueberryServerScheduler;
import net.blueberrymc.util.TinyTime;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class BlueberryClient extends BlueberryUtil {
    private static final Logger LOGGER = LogManager.getLogger();
    private final BlueberryClientScheduler clientScheduler = new BlueberryClientScheduler();
    private final BlueberryServerScheduler serverScheduler = new BlueberryServerScheduler();
    private final AtomicReference<Activity> discordRichPresenceQueue = new AtomicReference<>();
    @Nullable private final BlueberryClient impl;

    public BlueberryClient() {
        this(null);
    }

    public BlueberryClient(@Nullable BlueberryClient impl) {
        this.impl = impl;
    }

    @Override
    public @NotNull ResourceManager getResourceManager() {
        return Minecraft.getInstance().getResourceManager();
    }

    @Override
    public @NotNull CompletableFuture<Void> reloadResourcePacks() {
        return Minecraft.getInstance().reloadResourcePacks();
    }

    @Override
    public void crash(@NotNull CrashReport crashReport) {
        Preconditions.checkNotNull(crashReport, "crashReport cannot be null");
        Minecraft.fillReport(Minecraft.getInstance(), null, "unknown", null, crashReport);
        LOGGER.fatal(crashReport.getFriendlyReport());
        Minecraft.crash(crashReport);
    }

    @Override
    public @NotNull AbstractBlueberryScheduler getClientScheduler() {
        return clientScheduler;
    }

    @Override
    public @NotNull AbstractBlueberryScheduler getServerScheduler() {
        return serverScheduler;
    }

    @Override
    public boolean isOnGameThread() {
        return RenderSystem.isOnRenderThread()
                || Thread.currentThread().getName().equals("main")
                || Thread.currentThread().getName().equals("Server thread"); // may be called from integrated server (logical server)
    }

    // Blueberry
    // <details>
    // <state>
    // <start or end>
    @Override
    public void updateDiscordStatus(@Nullable String details, @Nullable String state, @Nullable SimpleEntry<String, String> bigImage, @Nullable SimpleEntry<String, String> smallImage, long start) {
        Activity activity = new Activity();
        if (details != null) {
            activity.setDetails(details);
        }
        if (state != null) {
            activity.setState(state);
        }
        if (bigImage != null) {
            activity.assets().setLargeImage(bigImage.getKey());
            activity.assets().setLargeText(bigImage.getValue());
        }
        if (smallImage != null) {
            activity.assets().setSmallImage(smallImage.getKey());
            activity.assets().setSmallText(smallImage.getValue());
        }
        if (start > 0) {
            activity.timestamps().setStart(Instant.ofEpochMilli(start));
        }
        setDiscordRichPresenceQueue(activity);
    }

    @Nullable
    @Override
    public Activity getDiscordRichPresenceQueue() {
        return discordRichPresenceQueue.get();
    }

    @Override
    public void setDiscordRichPresenceQueue(@Nullable Activity discordRichPresence) {
        discordRichPresenceQueue.set(discordRichPresence);
    }

    /**
     * Returns the integrated server if any.
     * @return integrated server or null if pre-init or not in single-player server
     */
    @SuppressWarnings("ConstantConditions") // it is null before init of Minecraft
    @Nullable
    public MinecraftServer getIntegratedServer() {
        return Minecraft.getInstance() != null ? Minecraft.getInstance().getSingleplayerServer() : null;
    }

    /**
     * Returns the implementation of BlueberryClient. Generally you shouldn't need to call this method.
     */
    @NotNull
    public BlueberryClient getImpl() {
        if (impl == null) throw new IllegalArgumentException("impl isn't defined (yet)");
        return impl;
    }

    /**
     * Registers the renderer for given block entity type.
     * @param blockEntityType block entity type
     * @param blockEntityRenderer renderer
     */
    public void registerSpecialBlockEntityRenderer(@NotNull BlockEntityType<?> blockEntityType, @NotNull BlockEntityRenderer<?> blockEntityRenderer) {
        ((MinecraftBlockEntityRenderDispatcher) Minecraft.getInstance().getBlockEntityRenderDispatcher()).registerSpecialRenderer(blockEntityType, blockEntityRenderer);
    }

    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreensFactory(@NotNull MenuType<? extends M> menuType, @NotNull ScreenConstructor<M, U> screenConstructor) {
        getImpl().registerMenuScreensFactory(menuType, screenConstructor);
    }

    /**
     * Shows warning screen about mod incompatibility (client is trying to join the world without previously installed mods)
     * @return false if world loading should be cancelled; true otherwise
     */
    public static boolean showIncompatibleWorldModScreen(@NotNull String levelId, @NotNull LevelStorageSource.LevelStorageAccess levelStorageAccess, @NotNull WorldStem worldStem, @NotNull Runnable runnable) {
        if (!ListUtils.isCompatibleVersionedModInfo(((InstalledModsContainer) worldStem.worldData()).getInstalledMods(), Blueberry.getModLoader().getActiveMods())) {
            Component title = new BlueberryText("blueberry", "selectWorld.backupQuestion.incompatibleMods").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
            Component description = new BlueberryText("blueberry", "selectWorld.backupWarning.incompatibleMods");
            List<Component> lines = new ArrayList<>();
            Set<SimpleEntry<VersionedModInfo, VersionedModInfo>> set = TinyTime.measureTime("getIncompatibleVersionedModInfo", () -> ListUtils.getIncompatibleVersionedModInfo(((InstalledModsContainer) worldStem.worldData()).getInstalledMods(), Blueberry.getModLoader().getActiveMods()));
            LOGGER.info("Mod incompatibility detected:");
            for (SimpleEntry<VersionedModInfo, VersionedModInfo> entry : set) {
                String key = Optional.ofNullable(entry.getKey()).map(i -> i.getName() + " [" + i.getModId() + "] @ " + i.getVersion()).orElse("");
                String value = Optional.ofNullable(entry.getValue()).map(i -> i.getName() + " [" + i.getModId() + "] @ " + i.getVersion()).orElse("");
                lines.add(new TextComponent(key + " -> " + value));
                LOGGER.info("  - {} -> {}", key, value);
            }
            Minecraft.getInstance().setScreen(new MultiLineBackupConfirmScreen(null, (backup, eraseCache) -> {
                if (backup) {
                    EditWorldScreen.makeBackupAndShowToast(Minecraft.getInstance().getLevelSource(), levelId);
                }
                runnable.run();
            }, title, description, false, lines));
            worldStem.close();
            try {
                levelStorageAccess.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to unlock access to level {}", levelId, e);
            }
            return false;
        }
        return true;
    }

    @FunctionalInterface
    public interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        @NotNull
        U create(@NotNull T menu, @NotNull Inventory inventory, @NotNull Component component);
    }
}
