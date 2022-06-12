package net.blueberrymc.world.level.block;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.world.level.block.state.BlockBehaviour;
import net.blueberrymc.world.level.block.state.BlockState;
import net.blueberrymc.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Blueberry equivalent of Minecraft Block.
 */
public abstract class BlockData extends BlockBehaviour {
    private final StateDefinition<BlockData, BlockState> stateDefinition;

    protected BlockData(@NotNull Properties properties) {
        super(properties);
        var builder = StateDefinition.<BlockData, BlockState>builder(this);
        createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(BlockData::defaultBlockState, BlockState::create);
    }

    @NotNull
    public StateDefinition<BlockData, BlockState> getStateDefinition() {
        return stateDefinition;
    }

    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<BlockData, BlockState> builder) {
    }

    /**
     * Create a new block data with the given minecraft block instance.
     * @param o the instance
     * @return the block data
     */
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static BlockData ofUnsafe(@NotNull Object o) {
        return (BlockData) ImplGetter.byConstructor(Object.class).apply(o);
    }

    /**
     * Returns the default block state.
     * @return block state
     */
    @NotNull
    public abstract BlockState defaultBlockState();

    /**
     * Gets if the block can have block entity.
     * @return true if block can have block entity; false otherwise.
     */
    public boolean isBlockEntity() {
        return false;
    }
}
