package net.blueberrymc.client.main;

import net.blueberrymc.common.launch.BlueberryTweaker;
import org.jetbrains.annotations.NotNull;

public class BlueberryClientTweaker extends BlueberryTweaker {
    @NotNull
    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }
}
