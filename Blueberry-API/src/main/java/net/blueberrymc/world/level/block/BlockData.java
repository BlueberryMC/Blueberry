package net.blueberrymc.world.level.block;

import net.blueberrymc.common.util.ImplGetter;
import net.blueberrymc.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface BlockData {
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    static BlockData ofUnsafe(@NotNull Object o) {
        return (BlockData) ImplGetter.byConstructor(Object.class).apply(o);
    }

    /**
     * Returns the default block state.
     * @return block state
     */
    @NotNull
    BlockState defaultBlockState();

    /**
     * Gets if the block can have block entity.
     * @return true if block can have block entity; false otherwise.
     */
    boolean isBlockEntity();
}
