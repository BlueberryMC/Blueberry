package net.blueberrymc.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CapturedBlock extends Block {
    private final BlockState blockState;

    public CapturedBlock(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState blockState) {
        super(level, pos);
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
    public net.minecraft.world.level.block.Block getBlock() {
        return getBlockStateOptional().map(BlockState::getBlock).orElseThrow(() -> new AssertionError("Block State is null"));
    }
}
