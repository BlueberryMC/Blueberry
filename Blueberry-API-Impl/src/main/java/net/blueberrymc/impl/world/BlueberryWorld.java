package net.blueberrymc.impl.world;

import net.blueberrymc.impl.util.PositionUtil;
import net.blueberrymc.impl.world.level.block.entity.BlueberryBlockEntity;
import net.blueberrymc.impl.world.level.block.state.BlueberryBlockState;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.Chunk;
import net.blueberrymc.world.World;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record BlueberryWorld(@NotNull Level handle) implements World {
    public BlueberryWorld {
        Objects.requireNonNull(handle, "handle");
    }

    @Override
    public @NotNull BlockState getBlockState(@NotNull Vec3i pos) {
        return new BlueberryBlockState(handle.getBlockState(PositionUtil.toBlockPos(pos)));
    }

    @Override
    public boolean setBlock(@NotNull Vec3i pos, @NotNull BlockState blockState, int flags) {
        return handle.setBlock(PositionUtil.toBlockPos(pos), ((BlueberryBlockState) blockState).handle(), flags);
    }

    @Override
    public boolean isLoaded(@NotNull Vec3i pos) {
        return handle.isLoaded(PositionUtil.toBlockPos(pos));
    }

    @Override
    public boolean isClientSide() {
        return handle.isClientSide();
    }

    @Override
    public int getLightEmission(@NotNull Vec3i pos) {
        return handle.getLightEmission(PositionUtil.toBlockPos(pos));
    }

    @Override
    public void notifyBlockChange(@NotNull Vec3i pos, @NotNull BlockState oldState, @NotNull BlockState newState, int flags) {
        handle.sendBlockUpdated(PositionUtil.toBlockPos(pos), ((BlueberryBlockState) oldState).handle(), ((BlueberryBlockState) newState).handle(), flags);
    }

    @Override
    public boolean removeBlock(@NotNull Vec3i pos, boolean notify) {
        return handle.removeBlock(PositionUtil.toBlockPos(pos), notify);
    }

    @Override
    public void removeBlockEntity(@NotNull Vec3i pos) {
        handle.removeBlockEntity(PositionUtil.toBlockPos(pos));
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(@NotNull Vec3i pos) {
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = handle.getBlockEntity(PositionUtil.toBlockPos(pos));
        if (blockEntity == null) {
            return null;
        }
        return new BlueberryBlockEntity(blockEntity);
    }

    @Override
    public @Nullable Chunk getChunkIfLoaded(int chunkX, int chunkZ) {
        if (handle.getChunkSource().hasChunk(chunkX, chunkZ)) {
            LevelChunk chunk = handle.getChunk(chunkX, chunkZ);
            return new BlueberryChunk(chunk);
        }
        return null;
    }

    @Contract("_, _ -> new")
    @Override
    public @NotNull Chunk getChunk(int chunkX, int chunkZ) {
        return new BlueberryChunk(handle.getChunk(chunkX, chunkZ));
    }
}
