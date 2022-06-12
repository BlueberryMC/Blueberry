package net.blueberrymc.world.level;

import net.blueberrymc.util.RandomSource;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LevelAccessor {
    @NotNull
    RandomSource getRandom();

    @Nullable
    BlockEntity getBlockEntity(@NotNull Vec3i pos);
}
