package net.blueberrymc.world.level.block.state;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface StateDefinition<O, S extends StateHolder<O, S>> {
    @NotNull
    O getOwner();

    @NotNull
    S any();

    @NotNull
    Collection<@NotNull S> getPossibleStates();

    @NotNull
    Collection<BlockState.Property<?>> getProperties();

    @Nullable
    BlockState.Property<?> getProperty(@NotNull String name);

    @Contract(pure = true)
    @NotNull
    static <O, S extends StateHolder<O, S>> Builder<O, S> builder(@NotNull O owner) {
        Objects.requireNonNull(owner);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    interface Builder<O, S extends StateHolder<O, S>> {
        @Contract("_ -> this")
        @NotNull
        Builder<O, S> add(@NotNull BlockState.Property<?>... properties);

        @Contract("_, _ -> new")
        @NotNull
        StateDefinition<O, S> create(@NotNull Function<O, S> defaultState, @NotNull Factory<O, S> factory);
    }

    @FunctionalInterface
    interface Factory<O, S extends StateHolder<O, S>> {
        @NotNull
        S create(@NotNull O owner, @NotNull Map<BlockState.Property<?>, Comparable<?>> values, @NotNull Object mapCodec);
    }
}
