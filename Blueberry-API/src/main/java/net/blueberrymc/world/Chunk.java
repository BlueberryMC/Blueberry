package net.blueberrymc.world;

import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Chunk {
    @NotNull
    World getWorld();

    /**
     * Gets the block state at the given position.
     * @param pos The position to get the block state at. x and z should be in the range of 0 to 15 (inclusive).
     * @return The block state at the given position.
     */
    @NotNull
    BlockState getBlockState(@NotNull Vec3i pos);

    /**
     * Gets the block entity at the given position.
     * @param pos The position to get the block entity at.
     * @return The block entity at the given position.
     */
    @Nullable
    BlockEntity getBlockEntity(@NotNull Vec3i pos);
}
