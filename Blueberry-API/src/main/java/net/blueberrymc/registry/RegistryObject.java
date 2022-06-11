package net.blueberrymc.registry;

import net.blueberrymc.common.util.LazyInitValue;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class RegistryObject<T> extends LazyInitValue<T> {
    @Nullable private Key key;

    public RegistryObject(@NotNull T value) {
        super(() -> Objects.requireNonNull(value));
    }

    public RegistryObject(@NotNull Supplier<T> sup) {
        super(Objects.requireNonNull(sup));
    }

    @Nullable
    public Key getKey() {
        return key;
    }

    @NotNull
    public RegistryObject<T> setKey(@Nullable Key key) {
        this.key = key;
        return this;
    }
}
