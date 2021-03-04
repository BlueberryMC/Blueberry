package net.blueberrymc.server;

import net.minecraft.SharedConstants;
import net.minecraft.server.Main;
import org.jetbrains.annotations.NotNull;

public class DebugMain {
    public static void main(@NotNull String@NotNull[] args) {
        SharedConstants.IS_RUNNING_IN_IDE = true;
        Main.main(args);
    }
}
