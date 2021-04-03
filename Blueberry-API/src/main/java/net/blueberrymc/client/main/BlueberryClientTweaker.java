package net.blueberrymc.client.main;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlueberryClientTweaker implements ITweaker {
    public static String[] args = null;

    @Override
    public void acceptOptions(List<String> list, File file, File file1, String s) {
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {
        MixinBootstrap.init();
        launchClassLoader.registerTransformer("org.spongepowered.asm.mixin.transformer.Proxy");
        launchClassLoader.registerTransformer("net.blueberrymc.common.util.BlueberryClassTransformer");
    }

    @NotNull
    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @NotNull
    @Override
    public String@NotNull[] getLaunchArguments() {
        List<String> a = new ArrayList<>(Arrays.asList(args));
        a.removeIf(s -> s.startsWith("--tweakClass"));
        return a.toArray(new String[0]);
    }
}
