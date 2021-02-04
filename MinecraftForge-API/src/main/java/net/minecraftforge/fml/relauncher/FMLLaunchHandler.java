package net.minecraftforge.fml.relauncher;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FMLLaunchHandler {
    @Contract(pure = true)
    @NotNull
    public static Side side() {
        return Blueberry.getSide();
    }
}
