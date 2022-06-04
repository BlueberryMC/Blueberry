package net.blueberrymc.impl.common;

import net.kyori.adventure.key.Key;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;

public record BlueberryRegistry<T>(
        @NotNull Registry<Object> registry,
        @NotNull Function<Object, T> valueMapper,
        @NotNull Function<T, Object> valueUnmapper) implements net.blueberrymc.common.Registry<T> {
    @SuppressWarnings("unchecked")
    @Contract("_, _, _ -> new")
    public static <T> @NotNull BlueberryRegistry<T> ofUnsafe(@NotNull String name, @NotNull Function<Object, T> valueMapper, @NotNull Function<T, Object> valueUnmapper) {
        try {
            Field field = Registry.class.getDeclaredField(name);
            if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) {
                throw new RuntimeException("Trying to get registry " + name + " which is not public static");
            }
            Object o = field.get(null);
            if (!(o instanceof Registry)) {
                throw new RuntimeException(name + " is not an instance of Registry");
            }
            return new BlueberryRegistry<>((Registry<Object>) o, valueMapper, valueUnmapper);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public T get(@Nullable Key key) {
        ResourceLocation location = null;
        if (key != null) {
            location = new ResourceLocation(key.namespace(), key.value());
        }
        return valueMapper.apply(registry.get(location));
    }

    @NotNull
    @Override
    public T getValueOrThrow(@NotNull Key key) {
        Objects.requireNonNull(key, "key");
        T value = get(key);
        if (value == null) {
            throw new IllegalArgumentException("No value found for key " + key);
        }
        return value;
    }

    @Nullable
    @Override
    public Key getKey(@NotNull T value) {
        return (Key) (Object) registry.getKey(valueUnmapper.apply(value));
    }
}
