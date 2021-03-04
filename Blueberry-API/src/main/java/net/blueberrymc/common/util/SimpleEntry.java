package net.blueberrymc.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Map;

public class SimpleEntry<K, V> extends AbstractMap.SimpleImmutableEntry<K, V> {
    public SimpleEntry(@Nullable K key, @Nullable V value) {
        super(key, value);
    }

    public SimpleEntry(@NotNull Map.@NotNull Entry<? extends K, ? extends V> entry) {
        super(entry);
    }

    @Contract("_, _ -> new")
    public static <K, V> @NotNull SimpleEntry<K, V> of(@Nullable K k, @Nullable V v) {
        return new SimpleEntry<>(k, v);
    }

    @Contract("_ -> new")
    public static <K, V> @NotNull SimpleEntry<K, V> key(@Nullable K k) {
        return of(k, null);
    }

    @Contract("_ -> new")
    public static <K, V> @NotNull SimpleEntry<K, V> value(@Nullable V v) {
        return of(null, v);
    }
}
