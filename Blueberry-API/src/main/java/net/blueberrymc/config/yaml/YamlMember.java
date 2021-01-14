package net.blueberrymc.config.yaml;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;

public interface YamlMember {
    @NotNull
    Yaml getYaml();

    @NotNull
    Object getRawData();

    default void save(@NotNull File file) throws IOException {
        YamlConfiguration.saveTo(file, getYaml(), this);
    }

    default void save(@NotNull String path) throws IOException {
        YamlConfiguration.saveTo(path, getYaml(), this);
    }

    @NotNull
    default String dump() {
        return YamlConfiguration.dump(this);
    }
}
