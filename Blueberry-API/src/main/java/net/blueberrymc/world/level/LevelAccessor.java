package net.blueberrymc.world.level;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public interface LevelAccessor {
    @NotNull
    Random getRandom();
}
