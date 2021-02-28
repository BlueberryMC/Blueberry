package net.blueberrymc.registry;

import net.blueberrymc.common.util.LazyInitValue;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class RegistryObject<T> extends LazyInitValue<T> {
    @Nullable private ResourceLocation resourceLocation;

    public RegistryObject(@NotNull T value) {
        super(() -> Objects.requireNonNull(value));
    }

    public RegistryObject(@NotNull Supplier<T> sup) {
        super(Objects.requireNonNull(sup));
    }

    @Nullable
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    @NotNull
    public RegistryObject<T> setResourceLocation(@Nullable ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
        return this;
    }
}
