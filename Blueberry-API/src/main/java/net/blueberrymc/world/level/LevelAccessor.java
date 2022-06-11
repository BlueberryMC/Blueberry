package net.blueberrymc.world.level;

import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public interface LevelAccessor {
    @NotNull
    Random getRandom();

    @Nullable
    BlockEntity getBlockEntity(@NotNull Vec3i pos);
}
