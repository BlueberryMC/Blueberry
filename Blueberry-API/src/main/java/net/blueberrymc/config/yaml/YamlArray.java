package net.blueberrymc.config.yaml;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class YamlArray extends ArrayList<Object> implements YamlMember {
    private final Yaml yaml;

    public YamlArray(@NotNull Yaml yaml, @Nullable List<?> list) {
        super();
        if (list != null) this.addAll(list);
        this.yaml = yaml;
    }

    public YamlArray(@Nullable List<?> list) {
        this(YamlConfiguration.DEFAULT, list);
    }

    public YamlArray(@NotNull Yaml yaml) { this(yaml, new ArrayList<>()); }

    public YamlArray() { this(YamlConfiguration.DEFAULT, new ArrayList<>()); }

    @NotNull
    @SuppressWarnings("unchecked")
    public YamlObject getObject(int index) { return new YamlObject(yaml, (Map<String, Object>) get(index)); }

    @NotNull
    @SuppressWarnings("unchecked")
    public YamlArray getArray(int index) { return new YamlArray(yaml, (List<Object>) get(index)); }

    @Contract
    public String getString(int index) { return (String) get(index); }

    public boolean getBoolean(int index) { return (boolean) get(index); }

    @Contract
    public Number getNumber(int index) { return (Number) get(index); }

    public int getInt(int index) { return getNumber(index).intValue(); }

    public float getFloat(int index) { return getNumber(index).floatValue(); }

    public double getDouble(int index) { return getNumber(index).doubleValue(); }

    public long getLong(int index) { return getNumber(index).longValue(); }

    public byte getByte(int index) { return getNumber(index).byteValue(); }

    public short getShort(int index) { return getNumber(index).shortValue(); }

    @SuppressWarnings("unchecked")
    public <T> void forEachAsType(@NotNull Consumer<T> action) { forEach(o -> action.accept((T) o)); }

    public void forEachIndexed(@NotNull BiConsumer<Object, Integer> action) {
        AtomicInteger index = new AtomicInteger();
        forEach(obj -> action.accept(obj, index.getAndIncrement()));
    }

    @SuppressWarnings("unchecked")
    public @NotNull <F, T> List<T> mapAsType(@NotNull BiFunction<F, Integer, T> function) {
        List<T> newList = new ArrayList<>();
        this.forEachIndexed((v, i) -> newList.add(function.apply((F) v, i)));
        return newList;
    }

    @SuppressWarnings("unchecked")
    public @NotNull <F, T> List<T> mapAsType(@NotNull Function<F, T> function) {
        List<T> newList = new ArrayList<>();
        this.forEach(v -> newList.add(function.apply((F) v)));
        return newList;
    }

    public @NotNull List<String> mapToString() {
        return mapAsType(o -> o instanceof String ? (String) o : o.toString());
    }

    @Override
    public @NotNull Yaml getYaml() { return yaml; }

    @Override
    public @NotNull Object getRawData() { return this; }
}
