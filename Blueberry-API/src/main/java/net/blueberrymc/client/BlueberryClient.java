package net.blueberrymc.client;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.blueberrymc.common.BlueberryUtil;
import net.blueberrymc.common.util.SimpleEntry;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class BlueberryClient implements BlueberryUtil {
    private final AtomicReference<DiscordRichPresence> discordRichPresenceQueue = new AtomicReference<>();

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
        discordRichPresenceQueue.set(
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
}
