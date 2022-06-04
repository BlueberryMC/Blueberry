package net.blueberrymc.world.level.block;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.Location;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.Chunk;
import net.blueberrymc.world.World;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a block that exists in the world at a specific location.
 */
public class Block {
    private final World world;
    private final Vec3i pos;

    public Block(@NotNull World world, @NotNull Vec3i pos) {
        Preconditions.checkNotNull(world, "level cannot be null");
        Preconditions.checkNotNull(pos, "pos cannot be null");
        this.world = world;
        this.pos = pos;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @NotNull
    public Vec3i getPos() {
        return pos;
    }

    @NotNull
    public Location getLocation() {
        return new Location(world, pos);
    }

    @NotNull
    public Chunk getChunk() {
        return getLocation().getChunk();
    }

    /**
     * Gets block state for this block. The result may be cached depending on the implementation. This method allows
     * subclasses to override.
     * @return the block state for this block
     */
    @Nullable
    public BlockState getBlockState() {
        return world.getBlockState(pos); // This implementation is non-null but subclasses might not be non-null
    }

    /**
     * Gets new block state for this block. This method cannot be overridden.
     * @return the fresh block state
     */
    @NotNull
    public final BlockState getFreshBlockState() {
        return world.getBlockState(pos);
    }

    @NotNull
    public Optional<BlockState> getBlockStateOptional() {
        return Optional.ofNullable(getBlockState());
    }

    public void removeBlockEntity() {
        world.removeBlockEntity(pos);
    }

    public boolean hasBlockEntity() {
        return getBlockEntity() != null;
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return world.getBlockEntity(pos);
    }

    /**
     * Gets block for this block. The result may be cached depending on the implementation. This method allows
     * subclasses to override.
     * @return the block
     */
    @Nullable
    public BlockData getBlockData() {
        return getBlockStateOptional().map(BlockState::getBlockData).orElse(null);
    }

    /**
     * Gets block for this block. This method cannot be overridden.
     * @return the block
     */
    @NotNull
    public final BlockData getFreshBlockData() {
        return getFreshBlockState().getBlockData();
    }

    public boolean removeBlock(boolean notify) {
        return world.removeBlock(pos, notify);
    }

    public boolean setBlock(@NotNull Block block) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return setBlock(block, true);
    }

    public boolean setBlock(@NotNull Block block, boolean applyPhysics) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return setBlockState(block.getBlockState(), applyPhysics);
    }

    public boolean setBlock(@NotNull BlockData block) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return setBlock(block, true);
    }

    public boolean setBlock(@NotNull BlockData block, boolean applyPhysics) {
        Preconditions.checkNotNull(block, "block cannot be null");
        return setBlockState(block.defaultBlockState(), applyPhysics);
    }

    public boolean setBlockState(@Nullable BlockState blockState) {
        return setBlockState(blockState, true);
    }

    public boolean setBlockState(@Nullable BlockState blockState, boolean applyPhysics) {
        if (blockState == null) blockState = Blocks.AIR.defaultBlockState();
        if (!blockState.isAir() && blockState.getBlockData() instanceof EntityBlock && !blockState.getBlockData().equals(this.getBlockData())) {
            world.removeBlockEntity(pos);
        }
        if (applyPhysics) {
            int flags = SetBlockFlags.UPDATE_NEIGHBOUR | SetBlockFlags.SEND_BLOCK_UPDATE;
            return world.setBlock(pos, blockState, flags);
        } else {
            BlockState old = getBlockState();
            Objects.requireNonNull(old, "old block state cannot be null");
            int flags = SetBlockFlags.SEND_BLOCK_UPDATE | SetBlockFlags.NO_OBSERVER;
            boolean success = world.setBlock(pos, blockState, flags);
            if (success) {
                world.notifyBlockChange(pos, old, blockState, SendBlockUpdateFlags.UPDATE_NEIGHBOUR | SendBlockUpdateFlags.SEND_BLOCK_UPDATE);
            }
            return success;
        }
    }

    public int getLightLevel() {
        return world.getLightEmission(pos);
    }

    @NotNull
    public Block getRelative(int modX, int modY, int modZ) {
        return new Block(world, pos.add(modX, modY, modZ));
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
        world.setBlock(pos, blockState, flags);
    }

    public boolean isLoaded() {
        return world.isLoaded(pos);
    }

    public boolean isClientSide() {
        return world.isClientSide();
    }

    public int getX() {
        return pos.x();
    }

    public int getY() {
        return pos.y();
    }

    public int getZ() {
        return pos.z();
    }
}
