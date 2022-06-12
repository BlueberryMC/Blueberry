package net.blueberrymc.world.level.block.state;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.world.level.block.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface BlockState extends StateHolder<BlockData, BlockState> {
    boolean isAir();

    @NotNull
    BlockData getBlockData();

    default boolean hasBlockEntity() {
        return getOwner().isBlockEntity();
    }

    @Contract(pure = true)
    @NotNull
    static BlockState create(@NotNull BlockData blockData, @NotNull Map<Property<?>, Comparable<?>> values, @NotNull Object mapCodec) {
        return (BlockState) ImplGetter.byMethod("create", BlockData.class, Map.class, Object.class).apply(blockData, values, mapCodec);
    }

    abstract class Property<T extends Comparable<T>> {
        private final String name;
        private final Class<T> type;

        protected Property(@NotNull String name, @NotNull Class<T> type) {
            this.name = name;
            this.type = type;
        }

        @NotNull
        public String name() {
            return name;
        }

        @NotNull
        public Class<T> type() {
            return type;
        }

        @NotNull
        public abstract Collection<T> getPossibleValues();

        public abstract String getName(@NotNull T value);

        @NotNull
        public abstract Optional<T> getValue(@NotNull String s);
    }
}
