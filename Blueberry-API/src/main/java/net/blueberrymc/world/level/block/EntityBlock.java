package net.blueberrymc.world.level.block;

import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a block that can have block entity.
 */
public interface EntityBlock extends BlockData {
    @Override
    default boolean isBlockEntity() {
        return true;
    }

    /**
     * Creates a block entity for this block.
     * @param pos position of the block entity
     * @param blockState block state
     * @return block entity
     */
    @NotNull
    BlockEntity newBlockEntity(@NotNull Vec3i pos, @NotNull BlockState blockState);
}
