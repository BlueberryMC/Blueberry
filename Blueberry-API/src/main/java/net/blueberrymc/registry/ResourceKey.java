package net.blueberrymc.registry;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("unused")
public interface ResourceKey<T> {
    @SuppressWarnings("unchecked")
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    static <T> ResourceKey<Registry<T>> createRegistryKey(@NotNull Key key) {
        return (ResourceKey<Registry<T>>) ImplGetter.byMethod("createRegistryKey", Key.class).apply(key);
    }

    @SuppressWarnings("unchecked")
    @Contract(value = "_ -> new", pure = true)
    @NotNull
    static <T> ResourceKey<T> ofUnsafeRegistryField(@NotNull String name) {
        return (ResourceKey<T>) ImplGetter.byMethod("ofUnsafeRegistryField", String.class).apply(name);
    }

    @NotNull Key registryName();

    @NotNull Key location();

    default boolean isFor(@NotNull ResourceKey<? extends Registry<?>> resourceKey) {
        return this.registryName().equals(resourceKey.location());
    }

    @SuppressWarnings("unchecked")
    default <E> @NotNull Optional<ResourceKey<E>> cast(@NotNull ResourceKey<? extends Registry<E>> resourceKey) {
        return this.isFor(resourceKey) ? Optional.of((ResourceKey<E>) this) : Optional.empty();
    }
}
