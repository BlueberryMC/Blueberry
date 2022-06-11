package net.blueberrymc.world.level.block;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.Location;
import net.blueberrymc.util.Vec3;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.Chunk;
import net.blueberrymc.world.World;
import net.blueberrymc.world.entity.Entity;
import net.blueberrymc.world.item.ItemStack;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

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

    // TODO: probably easier to delegate to ImplGetter
    @NotNull
    public static List<@NotNull ItemStack> getDrops(@NotNull BlockState state, @NotNull World world, @NotNull Vec3i pos, @Nullable BlockEntity blockEntity) {
        LootContext.Builder builder = LootContext.builder(world)
                .withRandom(world.getRandom())
                .withParameter(LootContextParams.ORIGIN, pos.toBlockVec3())
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        return state.getDrops(builder);
    }

    @NotNull
    public static List<@NotNull ItemStack> getDrops(@NotNull BlockState state, @NotNull World world, @NotNull Vec3i pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, @NotNull ItemStack itemStack) {
        LootContext.Builder builder = LootContext.builder(world)
                .withRandom(world.getRandom())
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, itemStack)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        return state.getDrops(builder);
    }

    public static void dropResources(@NotNull BlockState state, @NotNull World world, @NotNull Vec3i pos) {
        if (!world.isClientSide()) {
            getDrops(state, world, pos, null).forEach((itemStack) -> popResource(world, pos, itemStack));
            state.spawnAfterBreak(world, pos, ItemStack.EMPTY, true);
        }

    }

    public static void dropResources(@NotNull BlockState state, @NotNull World world, @NotNull Vec3i pos, @Nullable BlockEntity blockEntity) {
        if (!world.isClientSide()) {
            getDrops(state, world, pos, blockEntity).forEach((itemStack) -> popResource(world, pos, itemStack));
            state.spawnAfterBreak(world, pos, ItemStack.EMPTY, true);
        }

    }

    public static void dropResources(@NotNull BlockState state, @NotNull World world, @NotNull Vec3i pos, @Nullable BlockEntity blockEntity, @NotNull Entity entity, @NotNull ItemStack itemStack) {
        if (!world.isClientSide()) {
            getDrops(state, world, pos, blockEntity, entity, itemStack).forEach((itemStackx) -> popResource(world, pos, itemStackx));
            state.spawnAfterBreak(world, pos, itemStack, true);
        }

    }

    public static void popResource(@NotNull World world, @NotNull Vec3i pos, @NotNull ItemStack itemStack) {
        float divHeight = EntityType.ITEM.getHeight() / 2.0F;
        double x = (double) ((float) pos.x() + 0.5F) + Mth.nextDouble(world.getRandom(), -0.25D, 0.25D);
        double y = (double) ((float) pos.y() + 0.5F) + Mth.nextDouble(world.getRandom(), -0.25D, 0.25D) - (double) divHeight;
        double z = (double) ((float) pos.z() + 0.5F) + Mth.nextDouble(world.getRandom(), -0.25D, 0.25D);
        popResource(world, () -> new ItemEntity(world, x, y, z, itemStack), itemStack);
    }

    public static void popResourceFromFace(@NotNull World world, @NotNull Vec3i pos, @NotNull BlockFace direction, ItemStack itemStack) {
        int modX = direction.getModX();
        int modY = direction.getModY();
        int modZ = direction.getModZ();
        float divWidth = EntityType.ITEM.getWidth() / 2.0F;
        float divHeight = EntityType.ITEM.getHeight() / 2.0F;
        double d = (double) ((float) pos.x() + 0.5F) + (modX == 0 ? Mth.nextDouble(world.getRandom(), -0.25D, 0.25D) : (double) ((float) modX * (0.5F + divWidth)));
        double d2 = (double) ((float) pos.y() + 0.5F) + (modY == 0 ? Mth.nextDouble(world.getRandom(), -0.25D, 0.25D) : (double) ((float) modY * (0.5F + divHeight))) - (double) divHeight;
        double d3 = (double) ((float) pos.z() + 0.5F) + (modZ == 0 ? Mth.nextDouble(world.getRandom(), -0.25D, 0.25D) : (double) ((float) modZ * (0.5F + divWidth)));
        double d4 = modX == 0 ? Mth.nextDouble(world.getRandom(), -0.1D, 0.1D) : (double) modX * 0.1D;
        double d5 = modY == 0 ? Mth.nextDouble(world.getRandom(), 0.0D, 0.1D) : (double) modY * 0.1D + 0.1D;
        double d6 = modZ == 0 ? Mth.nextDouble(world.getRandom(), -0.1D, 0.1D) : (double) modZ * 0.1D;
        popResource(world, () -> new ItemEntity(world, d, d2, d3, itemStack, d4, d5, d6), itemStack);
    }

    private static void popResource(@NotNull World world, Supplier<ItemEntity> supplier, @NotNull ItemStack itemStack) {
        if (!world.isClientSide() && !itemStack.isEmpty() && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            ItemEntity itemEntity = (ItemEntity) supplier.get();
            itemEntity.setDefaultPickUpDelay();
            List<ItemEntity> drops = world.captureDrops.get();
            if (drops != null) {
                drops.add(itemEntity); return;
            } // Blueberry
            world.addFreshEntity(itemEntity);
        }
    }
}
