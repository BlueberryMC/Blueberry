package net.blueberrymc.world.level.block;

import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.World;
import net.blueberrymc.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CapturedBlock extends Block {
    private final BlockState blockState;

    public CapturedBlock(@NotNull World world, @NotNull Vec3i pos, @NotNull BlockState blockState) {
        super(world, pos);
        this.blockState = blockState;
    }

    /**
     * Gets block state for this captured block.
     * @return captured block state
     */
    @NotNull
    @Override
    public BlockState getBlockState() {
        return blockState;
    }

    /**
     * Gets block for this captured block.
     * @return the block
     */
    @NotNull
    @Override
    public BlockData getBlockData() {
        return getBlockStateOptional().map(BlockState::getBlockData).orElseThrow(() -> new AssertionError("Block State is null"));
    }
}
