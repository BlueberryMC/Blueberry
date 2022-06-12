package net.blueberrymc.impl.world.level.block;

import net.blueberrymc.impl.world.level.block.state.BlueberryBlockState;
import net.blueberrymc.util.Reflected;
import net.blueberrymc.world.level.block.BlockData;
import net.blueberrymc.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BlueberryBlockData extends BlockData {
    @NotNull
    private final Block handle;

    public BlueberryBlockData(@NotNull Block handle) {
        super(Properties.builder().build());
        this.handle = handle;
    }

    @Reflected
    public BlueberryBlockData(@NotNull Object o) {
        this((Block) o);
    }

    @NotNull
    public Block getHandle() {
        return handle;
    }

    @Contract(" -> new")
    @Override
    public @NotNull BlockState defaultBlockState() {
        return new BlueberryBlockState(handle.defaultBlockState());
    }

    @Override
    public boolean isBlockEntity() {
        return handle instanceof EntityBlock;
    }
}
