package net.blueberrymc.client;

import net.blueberrymc.common.BlueberryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public class BlueberryClient implements BlueberryUtil {
    @Override
    public @NotNull ResourceManager getResourceManager() {
        return Minecraft.getInstance().getResourceManager();
    }
}
