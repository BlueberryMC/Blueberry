package net.blueberrymc.impl.world.level.block;

import net.blueberrymc.impl.world.level.block.state.BlueberryBlockState;
import net.blueberrymc.util.Reflected;
import net.blueberrymc.world.level.block.BlockData;
import net.blueberrymc.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BlueberryBlockData(@NotNull Block handle) implements BlockData {
    @Reflected
    public BlueberryBlockData(@NotNull Object o) {
        this((Block) o);
    }

    public BlueberryBlockData {
        Objects.requireNonNull(handle, "handle");
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
