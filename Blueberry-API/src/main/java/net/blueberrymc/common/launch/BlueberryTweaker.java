package net.blueberrymc.common.launch;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BlueberryTweaker implements ITweaker {
    private static final Logger LOGGER = LogManager.getLogger();
    public static String[] args = null;

    @Override
    public void acceptOptions(@Nullable List<String> list, @Nullable File file, @Nullable File file1, @Nullable String s) {
    }

    @Override
    public void injectIntoClassLoader(@NotNull LaunchClassLoader launchClassLoader) {
        try {
            MixinBootstrap.init();
            launchClassLoader.registerTransformer("org.spongepowered.asm.mixin.transformer.Proxy");
            initMixin();
        } catch (Throwable throwable) {
            LOGGER.fatal("Critical error during initialization phase of mixin", throwable);
        }
        launchClassLoader.registerTransformer("net.blueberrymc.common.launch.BlueberryClassTransformer");
    }

    @NotNull
    @Override
    public abstract String getLaunchTarget();

    @NotNull
    @Override
    public String@NotNull[] getLaunchArguments() {
        List<String> a = new ArrayList<>(Arrays.asList(args));
        a.removeIf(s -> s.startsWith("--tweakClass"));
        return a.toArray(new String[0]);
    }

    private static boolean initializedMixin = false;

    public static void initMixin() {
        if (initializedMixin) throw new RuntimeException("Mixin is already initialized");
        try {
            Method m = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            m.setAccessible(true);
            m.invoke(null, MixinEnvironment.Phase.INIT);
            m.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        initializedMixin = true;
    }
}
