package net.blueberrymc.impl.world.level.block.state;

import net.blueberrymc.impl.world.level.block.BlueberryBlockData;
import net.blueberrymc.world.level.block.BlockData;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BlueberryBlockState(@NotNull BlockState handle) implements net.blueberrymc.world.level.block.state.BlockState {
    public BlueberryBlockState {
        Objects.requireNonNull(handle, "handle");
    }

    @Override
    public boolean isAir() {
        return handle.isAir();
    }

    @Override
    public @NotNull BlockData getBlockData() {
        return new BlueberryBlockData(handle.getBlock());
    }
}
