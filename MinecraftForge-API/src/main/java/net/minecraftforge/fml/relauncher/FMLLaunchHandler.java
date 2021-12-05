package net.minecraftforge.fml.relauncher;

import net.blueberrymc.common.Side;
import net.blueberrymc.server.main.ServerMain;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class FMLLaunchHandler {
    @Contract(pure = true)
    @NotNull
    public static Side side() {
        return Side.valueOf(((String) ServerMain.blackboard.get("side")).toUpperCase(Locale.ROOT));
    }
}
