package net.blueberrymc.world;

import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface World {
    /**
     * Gets the block state at the given position.
     * @param pos The position to get the block state at.
     * @return The block state at the given position.
     */
    @NotNull
    BlockState getBlockState(@NotNull Vec3i pos);

    /**
     * Sets the block state at the given position.
     * @param pos The position to set the block state at.
     * @param blockState The block state to set.
     * @param flags The flags to use when setting the block state.
     * @return Whether the block state was successfully set.
     */
    boolean setBlock(@NotNull Vec3i pos, @NotNull BlockState blockState, int flags);

    /**
     * Checks if block at the given position is loaded.
     * @param pos The position to check.
     * @return true is loaded; false otherwise.
     */
    boolean isLoaded(@NotNull Vec3i pos);

    /**
     * Gets if this world represents a client world.
     * <p>
     * This method returns true if:
     * <ul>
     *     <li>running on the logical client</li>
     * </ul>
     * <p>
     * This method returns false if:
     * <ul>
     *     <li>running on the logical server in physical client</li>
     *     <li>running on the physical server</li>
     * </ul>
     * @return true if this world is running on the logical client; false otherwise.
     */
    boolean isClientSide();

    /**
     * Gets the light emission (also known as "light level") at the given position.
     * @param pos The position to get the light emission at.
     * @return The light emission at the given position.
     */
    int getLightEmission(@NotNull Vec3i pos);

    /**
     * Notifies the world that a block has changed. This affects the path navigation.
     * @param pos The position of the block that has changed.
     * @param oldState The old block state.
     * @param newState The new block state.
     * @param flags The flags to use when notifying the world.
     */
    void notifyBlockChange(@NotNull Vec3i pos, @NotNull BlockState oldState, @NotNull BlockState newState, int flags);

    /**
     * Removes a block at the given position but keeps the fluid as-is.
     * @param pos The position to remove the block at.
     * @param notify Whether to notify the surrounding blocks.
     * @return true if block was removed; false otherwise.
     */
    boolean removeBlock(@NotNull Vec3i pos, boolean notify);

    /**
     * Removes a block entity at the given position.
     * @param pos The position to remove the block entity at.
     */
    void removeBlockEntity(@NotNull Vec3i pos);

    /**
     * Gets the block entity at the given position.
     * @param pos The position to get the block entity at.
     * @return The block entity at the given position.
     */
    @Nullable
    BlockEntity getBlockEntity(@NotNull Vec3i pos);

    /**
     * Gets the loaded chunk at the given position.
     * @param chunkX The chunk X position.
     * @param chunkZ The chunk Z position.
     * @return chunk if loaded; null otherwise.
     */
    @Nullable
    Chunk getChunkIfLoaded(int chunkX, int chunkZ);

    /**
     * Gets the chunk at the given position. Unlike {@link #getChunkIfLoaded(int, int)}, this method tries to load or
     * generate the chunk if it does not exist.
     * @param chunkX The chunk X position.
     * @param chunkZ The chunk Z position.
     * @return chunk
     */
    @Contract("_, _ -> new")
    @NotNull
    Chunk getChunk(int chunkX, int chunkZ);
}
