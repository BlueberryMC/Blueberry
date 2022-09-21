package net.blueberrymc.impl.world;

import net.blueberrymc.impl.util.PositionUtil;
import net.blueberrymc.impl.world.level.block.entity.BlueberryBlockEntity;
import net.blueberrymc.impl.world.level.block.state.BlueberryBlockState;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.Chunk;
import net.blueberrymc.world.World;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BlueberryChunk(@NotNull LevelChunk handle) implements Chunk {
    @Contract(" -> new")
    @Override
    public @NotNull World getWorld() {
        return new BlueberryWorld(handle.getLevel());
    }

    @Contract("_ -> new")
    @Override
    public @NotNull BlockState getBlockState(@NotNull Vec3i pos) {
        return new BlueberryBlockState(handle.getBlockState(PositionUtil.toBlockPos(pos)));
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(@NotNull Vec3i pos) {
        var be = handle.getBlockEntity(PositionUtil.toBlockPos(pos));
        if (be == null) {
            return null;
        }
        return new BlueberryBlockEntity(be);
    }
}
