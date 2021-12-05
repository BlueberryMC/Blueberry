package net.blueberrymc.server.main;

import net.blueberrymc.common.launch.BlueberryTweaker;
import org.jetbrains.annotations.NotNull;

public class BlueberryServerTweaker extends BlueberryTweaker {
    @NotNull
    @Override
    public String getLaunchTarget() {
        return "net.minecraft.server.Main";
    }
}
