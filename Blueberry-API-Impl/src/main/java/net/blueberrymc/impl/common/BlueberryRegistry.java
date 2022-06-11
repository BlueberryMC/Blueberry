package net.blueberrymc.impl.common;

import net.blueberrymc.impl.util.KeyUtil;
import net.blueberrymc.util.Reflected;
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
        @NotNull Registry<Object> handle,
        @NotNull Function<Object, T> valueMapper,
        @NotNull Function<T, Object> valueUnmapper) implements net.blueberrymc.common.Registry<T> {
    @SuppressWarnings("unchecked")
    @Reflected
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

    public static <T> @NotNull Object register(@NotNull net.blueberrymc.common.Registry<T> registry, @NotNull Key location, @Nullable Object object) {
        return Registry.register(((BlueberryRegistry<T>) registry).handle, KeyUtil.toMinecraft(location), registry.valueUnmapper().apply(object));
    }

    @Nullable
    @Override
    public T get(@Nullable Key key) {
        ResourceLocation location = null;
        if (key != null) {
            location = new ResourceLocation(key.namespace(), key.value());
        }
        return valueMapper.apply(handle.get(location));
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
        return (Key) (Object) handle.getKey(valueUnmapper.apply(value));
    }

    @Override
    public @Nullable T byId(int id) {
        return valueMapper.apply(handle.byId(id));
    }

    @Override
    public int getId(@NotNull T value) {
        return handle.getId(valueUnmapper.apply(value));
    }
}
