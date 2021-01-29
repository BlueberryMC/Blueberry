package net.blueberrymc.config;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.config.yaml.YamlConfiguration;
import net.blueberrymc.config.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class ModConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    @NotNull private final String filename;
    @NotNull private final ModDescriptionFile modDescriptionFile;
    @NotNull private YamlObject config;
    @NotNull private final File configFile;

    private ModConfig(@NotNull String filename, @NotNull ModDescriptionFile modDescriptionFile) {
        this.filename = filename;
        this.modDescriptionFile = modDescriptionFile;
        try {
            this.configFile = new File(Blueberry.getModLoader().getConfigDir(), filename);
            if (!this.configFile.exists() && !this.configFile.createNewFile()) {
                LOGGER.warn("Could not create config file " + configFile.getName());
            }
            if (!this.configFile.isFile()) {
                throw new IOException(configFile.getName() + " is not a file");
            }
            this.config = new YamlConfiguration(this.configFile).asObject();
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + filename, e);
        }
    }

    public ModConfig(@NotNull ModDescriptionFile modDescriptionFile) {
        this(getDefaultFileName(modDescriptionFile.getModId()), modDescriptionFile);
    }

    private static String getDefaultFileName(String modId) {
        return String.format("%s.yml", modId);
    }

    @NotNull
    public String getModId() {
        return modDescriptionFile.modId;
    }

    @NotNull
    public String getFilename() {
        return filename;
    }

    @NotNull
    public ModDescriptionFile getModDescriptionFile() {
        return modDescriptionFile;
    }

    @NotNull
    public YamlObject getConfig() {
        return config;
    }

    @NotNull
    public File getConfigFile() {
        return configFile;
    }

    public void reloadConfig() throws IOException {
        this.config = new YamlConfiguration(this.configFile).asObject();
    }

    public void saveConfig() throws IOException {
        config.save(this.configFile);
    }

    public void set(@NotNull("path") String path, @Nullable Object value) {
        YamlObject parent = null;
        YamlObject object = getConfig();
        String[] arr = path.split("\\.");
        for (int i = 0; i < arr.length; i++) {
            boolean last = i + 1 == arr.length;
            if (last) {
                object.set(arr[i], value);
                if (parent != null) parent.setObject(arr[i - 1], object);
            } else {
                parent = object;
                object = getOrCreateObject(object, arr[i]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@NotNull("path") String path, @Nullable T def) {
        YamlObject object = getConfig();
        String[] arr = path.split("\\.");
        for (int i = 0; i < arr.length; i++) {
            boolean last = i + 1 == arr.length;
            if (last) {
                T result;
                try {
                    result = (T) object.getRawData().get(arr[i]);
                } catch (ClassCastException ex) {
                    return def;
                }
                if (result == null) return def;
                return result;
            } else {
                object = object.getObject(arr[i]);
                if (object == null) return def;
            }
        }
        return def;
    }

    public boolean getBoolean(@NotNull("path") String path) {
        return getBoolean(path, false);
    }

    public boolean getBoolean(@NotNull("path") String path, boolean def) {
        return get(path, def);
    }

    public String getString(@NotNull("path") String path) {
        return getString(path, null);
    }

    public String getString(@NotNull("path") String path, @Nullable String def) {
        return get(path, def);
    }

    public float getFloat(@NotNull("path") String path) {
        return getFloat(path, 0.0F);
    }

    public float getFloat(@NotNull("path") String path, float def) {
        return (float) getDouble(path, def);
    }

    public double getDouble(@NotNull("path") String path) {
        return getDouble(path, 0.0D);
    }

    public double getDouble(@NotNull("path") String path, double def) {
        return get(path, def);
    }

    public int getInt(@NotNull("path") String path) {
        return getInt(path, 0);
    }

    public int getInt(@NotNull("path") String path, int def) {
        return get(path, def);
    }

    public long getLong(@NotNull("path") String path) {
        return getLong(path, 0);
    }

    public long getLong(@NotNull("path") String path, long def) {
        return get(path, def);
    }

    public byte getByte(@NotNull("path") String path) {
        return getByte(path, (byte) 0);
    }

    public byte getByte(@NotNull("path") String path, byte def) {
        return (byte) getInt(path, def);
    }

    public short getShort(@NotNull("path") String path) {
        return getShort(path, (short) 0);
    }

    public short getShort(@NotNull("path") String path, short def) {
        return (short) getInt(path, def);
    }

    public YamlObject getOrCreateObject(@NotNull("parent") YamlObject parent, @NotNull("path") String path) {
        YamlObject obj = parent.getObject(path);
        if (obj == null) {
            obj = new YamlObject();
            parent.setObject(path, obj);
        }
        return obj;
    }
}
