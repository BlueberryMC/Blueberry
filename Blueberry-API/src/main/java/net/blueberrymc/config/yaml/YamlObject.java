package net.blueberrymc.config.yaml;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlObject implements YamlMember {
    public static final YamlMember NULL = new YamlObject();
    private final Map<String, Object> map;
    private final Yaml yaml;

    @Contract(pure = true)
    public YamlObject(@NotNull Yaml yaml, @Nullable Map<String, Object> map) {
        this.yaml = yaml;
        this.map = map == null ? new HashMap<>() : map;
    }

    @Contract(pure = true)
    public YamlObject(@NotNull Yaml yaml) { this(yaml, new HashMap<>()); }

    @Contract(pure = true)
    public YamlObject(@Nullable Map<String, Object> map) { this(YamlConfiguration.DEFAULT, map); }

    @Contract(pure = true)
    public YamlObject() { this(YamlConfiguration.DEFAULT); }

    // cannot put 'null', instead, it will remove the entry
    public void set(@NotNull String key, @Nullable Object value) {
        if (value instanceof YamlMember) {
            set(key, ((YamlMember) value).getRawData());
            return;
        }
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value == NULL ? null : value);
        }
    }

    // can put 'null'
    public void setNullable(@NotNull String key, @Nullable Object value) {
        map.put(key, value == NULL ? null : value);
    }

    public void setObject(@NotNull String key, @Nullable YamlObject object) {
        set(key, object == null ? null : object.getRawData());
    }

    public void setNullableObject(@NotNull String key, @Nullable YamlObject object) {
        setNullable(key, object == null ? null : object.getRawData());
    }

    @NotNull
    public YamlObject thenSet(@NotNull String key, @Nullable Object value) {
        set(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public YamlObject getObject(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (!this.map.containsKey(key)) return null;
        Object o = this.map.get(key);
        if (o instanceof Map) {
            return new YamlObject((Map<String, Object>) o);
        } else {
            return new YamlObject();
        }
    }

    public YamlArray getArray(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        if (!this.map.containsKey(key)) return null;
        Object o = this.map.get(key);
        if (o instanceof List) {
            return new YamlArray(yaml, (List<?>) o);
        } else {
            return new YamlArray(yaml);
        }
    }

    public String getString(@NotNull String key) {
        if (!this.map.containsKey(key) || this.map.get(key) == null) return null;
        return this.map.get(key).toString();
    }

    public String getString(@NotNull String key, @Nullable String def) {
        String s = getString(key);
        return s == null ? def : s;
    }

    public boolean getBoolean(@NotNull String key) { return getBoolean(key, false); }

    public boolean getBoolean(@NotNull String key, boolean def) {
        if (!this.map.containsKey(key) || this.map.get(key) == null) return def;
        Object o = this.map.get(key);
        if (o instanceof Boolean) return (boolean) o;
        return def;
    }

    public Number getNumber(@NotNull String key) {
        if (!this.map.containsKey(key) || this.map.get(key) == null) return null;
        Object o = this.map.get(key);
        if (o instanceof Number) return (Number) o;
        return null;
    }

    public Number getNumber(@NotNull String key, Number def) {
        Number number = getNumber(key);
        return number == null ? def : number;
    }

    public int getInt(@NotNull String key) { return getInt(key, 0); }

    public int getInt(@NotNull String key, int def) { return getNumber(key, def).intValue(); }

    public float getFloat(@NotNull String key) { return getFloat(key, 0); }

    public float getFloat(@NotNull String key, float def) { return getNumber(key, def).floatValue(); }

    public long getLong(@NotNull String key) { return getLong(key, 0); }

    public long getLong(@NotNull String key, long def) { return getNumber(key, def).longValue(); }

    public double getDouble(@NotNull String key) { return getDouble(key, 0); }

    public double getDouble(@NotNull String key, double def) { return getNumber(key, def).doubleValue(); }

    public byte getByte(@NotNull String key) { return getByte(key, (byte) 0); }

    public byte getByte(@NotNull String key, byte def) { return getNumber(key, def).byteValue(); }

    public short getShort(@NotNull String key) { return getShort(key, (short) 0); }

    public short getShort(@NotNull String key, short def) { return getNumber(key, def).shortValue(); }

    @Override
    public @NotNull Yaml getYaml() { return yaml; }

    @Override
    public @NotNull Map<String, Object> getRawData() { return map; }
}
