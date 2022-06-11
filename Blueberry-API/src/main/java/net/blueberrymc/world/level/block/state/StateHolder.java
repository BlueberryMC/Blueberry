package net.blueberrymc.world.level.block.state;

import org.jetbrains.annotations.NotNull;

/**
 * A holder for a states.
 * @param <O> owner type
 * @param <S> state type
 */
public interface StateHolder<O, S extends StateHolder<O, S>> {
    <V extends Comparable<V>> @NotNull BlockState setValue(@NotNull BlockState.Property<V> property, @NotNull V value);

    <V extends Comparable<V>> @NotNull V getValue(@NotNull BlockState.Property<V> property);

    @NotNull
    O getOwner();
}
