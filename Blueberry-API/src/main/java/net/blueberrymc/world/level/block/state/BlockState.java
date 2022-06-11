package net.blueberrymc.world.level.block.state;

import net.blueberrymc.world.item.ItemStack;
import net.blueberrymc.world.level.block.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BlockState extends StateHolder<BlockData, BlockState> {
    boolean isAir();

    @NotNull
    BlockData getBlockData();

    default boolean hasBlockEntity() {
        return getOwner().isBlockEntity();
    }

    @NotNull
    List<@NotNull ItemStack> getDrops(@NotNull LootContext.Builder builder);

    record Property<V extends Comparable<V>>(@NotNull String name, @NotNull Class<V> type) {
        @Contract("_, _ -> new")
        public static <V extends Comparable<V>> @NotNull Property<V> create(@NotNull String name, @NotNull Class<V> type) {
            return new Property<>(name, type);
        }
    }
}
