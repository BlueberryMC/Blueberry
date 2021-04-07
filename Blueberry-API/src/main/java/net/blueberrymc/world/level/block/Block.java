package net.blueberrymc.world.level.block;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.Location;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class Block {
    private final Level level;
    private final BlockPos pos;

    public Block(@NotNull Level level, @NotNull BlockPos pos) {
        Preconditions.checkNotNull(level, "level cannot be null");
        Preconditions.checkNotNull(pos, "pos cannot be null");
        this.level = level;
        this.pos = pos;
    }

    @NotNull
    public Level getLevel() {
        return level;
    }

    @NotNull
    public BlockPos getPos() {
        return pos;
    }

    @NotNull
    public Location getLocation() {
        return new Location(level, pos);
    }

    @NotNull
    public LevelChunk getChunk() {
        return Objects.requireNonNull(getLocation().getChunk());
    }

    /**
     * Gets block state for this block. The result may be cached depending on the implementation. This method allows
     * subclasses to override.
     * @return the block state for this block
     */
    @Nullable
    public BlockState getBlockState() {
        return level.getBlockState(pos);
    }

    /**
     * Gets new block state for this block. This method cannot be overridden.
     * @return the fresh block state
     */
    @Nullable
    public final BlockState getFreshBlockState() {
        return level.getBlockState(pos);
    }

    @NotNull
    public Optional<BlockState> getBlockStateOptional() {
        return Optional.ofNullable(getBlockState());
    }

    public void removeBlockEntity() {
        level.removeBlockEntity(pos);
    }

    public boolean hasBlockEntity() {
        return getBlockEntity() != null;
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return level.getBlockEntity(pos);
    }

    /**
     * Gets block for this block. The result may be cached depending on the implementation. This method allows
     * subclasses to override.
     * @return the block
     */
    @Nullable
    public net.minecraft.world.level.block.Block getBlock() {
        return getBlockStateOptional().map(BlockState::getBlock).orElse(null);
    }

    /**
     * Gets block for this block. This method cannot be overridden.
     * @return the block
     */
    @Nullable
    public final net.minecraft.world.level.block.Block getFreshBlock() {
        return Optional.ofNullable(level.getBlockState(pos)).map(BlockState::getBlock).orElse(null);
    }

    public boolean removeBlock(boolean notify) {
        return level.removeBlock(pos, notify);
    }

    public boolean setBlock(@NotNull Block block) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return setBlock(block, true);
    }

    public boolean setBlock(@NotNull Block block, boolean applyPhysics) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return setBlockState(block.getBlockState(), applyPhysics);
    }

    public boolean setBlock(@NotNull net.minecraft.world.level.block.Block block) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return setBlock(block, true);
    }

    public boolean setBlock(@NotNull net.minecraft.world.level.block.Block block, boolean applyPhysics) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return setBlockState(block.defaultBlockState(), applyPhysics);
    }

    public boolean setBlockState(@Nullable BlockState blockState) {
        return setBlockState(blockState, true);
    }

    public boolean setBlockState(@Nullable BlockState blockState, boolean applyPhysics) {
        if (blockState == null) blockState = Blocks.AIR.defaultBlockState();
        if (!blockState.isAir() && blockState.getBlock() instanceof EntityBlock && !blockState.getBlock().equals(this.getBlock())) {
            level.removeBlockEntity(pos);
        }
        if (applyPhysics) {
            int flags = SetBlockFlags.UPDATE_NEIGHBOUR | SetBlockFlags.SEND_BLOCK_UPDATE;
            return level.setBlock(pos, blockState, flags);
        } else {
            BlockState old = getBlockState();
            int flags = SetBlockFlags.SEND_BLOCK_UPDATE | SetBlockFlags.NO_OBSERVER;
            boolean success = level.setBlock(pos, blockState, flags);
            if (success) {
                level.sendBlockUpdated(pos, old, blockState, SendBlockUpdateFlags.UPDATE_NEIGHBOUR | SendBlockUpdateFlags.SEND_BLOCK_UPDATE);
            }
            return success;
        }
    }

    public int getLightLevel() {
        return level.getLightEmission(pos);
    }

    @NotNull
    public Block getRelative(int modX, int modY, int modZ) {
        return new Block(level, new BlockPos(getX() + modX, getY() + modY, getZ() + modZ));
    }

    @NotNull
    public Block getRelative(@NotNull BlockFace face) {
        return getRelative(face, 1);
    }

    @NotNull
    public Block getRelative(@NotNull BlockFace face, int distance) {
        Preconditions.checkNotNull(face, "face cannot be null");
        return getRelative(face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
    }

    @Nullable
    public BlockFace getFace(@NotNull Block block) {
        for (BlockFace face : BlockFace.values()) {
            if ((this.getX() + face.getModX() == block.getX()) && (this.getY() + face.getModY() == block.getY()) && (this.getZ() + face.getModZ() == block.getZ())) {
                return face;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Block{" +
                "location=" + getLocation().toBlockString() +
                ", block=" + getBlockState() +
                ", isLoaded=" + isLoaded() +
                '}';
    }

    public void setBlockState(@NotNull BlockState blockState, @MagicConstant(flagsFromClass = SetBlockFlags.class) int flags) {
        Preconditions.checkNotNull(blockState, "blockState cannot be null");
        level.setBlock(pos, blockState, flags);
    }

    public boolean isLoaded() {
        return level.isLoaded(pos);
    }

    public boolean isClientSide() {
        return level.isClientSide;
    }

    public int getX() {
        return pos.getX();
    }

    public int getY() {
        return pos.getY();
    }

    public int getZ() {
        return pos.getZ();
    }
}
