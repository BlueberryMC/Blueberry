package net.blueberrymc.client.main;

import net.blueberrymc.common.launchwrapper.Launch;
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
        Launch.main(newArgs);
    }
}
