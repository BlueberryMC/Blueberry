package net.blueberrymc.server;

import net.minecraft.SharedConstants;
import net.minecraft.server.Main;

public class DebugMain {
    public static void main(String[] args) {
        SharedConstants.IS_RUNNING_IN_IDE = true;
        Main.main(args);
    }
}
