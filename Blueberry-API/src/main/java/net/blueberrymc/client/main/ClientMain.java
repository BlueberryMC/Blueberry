package net.blueberrymc.client.main;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.SharedConstants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientMain {
    public static void main(@NotNull String@NotNull[] args) {
        List<String> arguments = new ArrayList<>();
        arguments.add("--tweakClass=net.blueberrymc.client.main.BlueberryClientTweaker");
        arguments.addAll(Arrays.asList(args));
        if (arguments.contains("--debug")) SharedConstants.IS_RUNNING_IN_IDE = true;
        String[] newArgs = arguments.toArray(new String[0]);
        BlueberryClientTweaker.args = newArgs;
        preloadClasses();
        Launch.main(newArgs);
    }

    private static void preloadClasses() {
        preloadClass("com.sun.jna.Structure");
        preloadClass("com.sun.jna.platform.win32.WinNT$OSVERSIONINFOEX");
        preloadClass("com.sun.jna.platform.win32.VersionHelpers");
    }

    private static void preloadClass(String clazz) {
        try {
            Class.forName(clazz);
        } catch (ClassNotFoundException ignore) {}
    }
}
