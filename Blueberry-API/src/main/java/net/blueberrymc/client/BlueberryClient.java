package net.blueberrymc.client;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.blueberrymc.client.renderer.blockentity.MinecraftBlockEntityRenderDispatcher;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.SideOnly;
import net.blueberrymc.common.util.SimpleEntry;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

@SideOnly(Side.CLIENT)
public class BlueberryClient implements BlueberryUtil {
    private final AtomicReference<DiscordRichPresence> discordRichPresenceQueue = new AtomicReference<>();
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
    public void reloadResourcePacks() {
        Minecraft.getInstance().reloadResourcePacks();
    }

    @Override
    public void crash(@NotNull CrashReport crashReport) {
        Preconditions.checkNotNull(crashReport, "crashReport cannot be null");
        Minecraft.fillReport(null, null, null, crashReport);
        Minecraft.crash(crashReport);
    }

    @Override
    public boolean isOnGameThread() {
        return RenderSystem.isOnRenderThread();
    }

    // Blueberry
    // <details>
    // <state>
    // <start or end>
    @Override
    public void updateDiscordStatus(@Nullable String details, @Nullable String state, @Nullable SimpleEntry<String, String> bigImage, @Nullable SimpleEntry<String, String> smallImage, long start) {
        setDiscordRichPresenceQueue(
                new DiscordRichPresence
                        .Builder(state)
                        .setDetails(details)
                        .setBigImage(bigImage != null ? bigImage.getKey() : null, bigImage != null ? bigImage.getValue() : null)
                        .setSmallImage(smallImage != null ? smallImage.getKey() : null, smallImage != null ? smallImage.getValue() : null)
                        .setStartTimestamps(start)
                        .build()
        );
    }

    @Nullable
    @Override
    public DiscordRichPresence getDiscordRichPresenceQueue() {
        return discordRichPresenceQueue.get();
    }

    @Override
    public void setDiscordRichPresenceQueue(@Nullable DiscordRichPresence discordRichPresence) {
        discordRichPresenceQueue.set(discordRichPresence);
    }

    @Nullable
    public MinecraftServer getIntegratedServer() {
        return Minecraft.getInstance().getSingleplayerServer();
    }

    @NotNull
    public BlueberryClient getImpl() {
        if (impl == null) throw new IllegalArgumentException("impl isn't defined (yet)");
        return impl;
    }

    public void registerSpecialBlockEntityRenderer(@NotNull BlockEntityType<?> blockEntityType, @NotNull BlockEntityRenderer<?> blockEntityRenderer) {
        ((MinecraftBlockEntityRenderDispatcher) Minecraft.getInstance().getBlockEntityRenderDispatcher()).registerSpecialRenderer(blockEntityType, blockEntityRenderer);
    }

    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreensFactory(@NotNull MenuType<? extends M> menuType, @NotNull ScreenConstructor<M, U> screenConstructor) {
        getImpl().registerMenuScreensFactory(menuType, screenConstructor);
    }

    public interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        U create(T menu, Inventory inventory, Component component);
    }
}
