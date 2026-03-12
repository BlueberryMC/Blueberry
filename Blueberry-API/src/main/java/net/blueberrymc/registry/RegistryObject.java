package net.blueberrymc.registry;

import net.blueberrymc.common.util.LazyInitValue;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class RegistryObject<T> extends LazyInitValue<T> {
    @Nullable private Identifier Identifier;

    public RegistryObject(@NotNull T value) {
        super(() -> Objects.requireNonNull(value));
    }

    public RegistryObject(@NotNull Supplier<T> sup) {
        super(Objects.requireNonNull(sup));
    }

    @Nullable
    public Identifier getResourceLocation() {
        return Identifier;
    }

    @NotNull
    public RegistryObject<T> setResourceLocation(@Nullable Identifier Identifier) {
        this.Identifier = Identifier;
        return this;
    }
}
