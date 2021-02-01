package net.blueberrymc.client;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.BlueberryUtil;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public class BlueberryClient implements BlueberryUtil {
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
}
