package net.blueberrymc.common.util;

import java.util.AbstractMap;
import java.util.Map;

public class SimpleEntry<K, V> extends AbstractMap.SimpleImmutableEntry<K, V> {
    public SimpleEntry(K key, V value) {
        super(key, value);
    }

    public SimpleEntry(Map.Entry<? extends K, ? extends V> entry) {
        super(entry);
    }

    public static <K, V> SimpleEntry<K, V> of(K k, V v) {
        return new SimpleEntry<>(k, v);
    }

    public static <K, V> SimpleEntry<K, V> key(K k) {
        return of(k, null);
    }

    public static <K, V> SimpleEntry<K, V> value(V v) {
        return of(null, v);
    }
}
