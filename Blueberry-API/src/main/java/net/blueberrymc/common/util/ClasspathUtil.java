package net.blueberrymc.common.util;

import net.blueberrymc.common.bml.InvalidModException;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;

public class ClasspathUtil {
    public static @NotNull String getClasspath(@NotNull Class<?> clazz) {
        String path;
        try {
            path = clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            throw new InvalidModException(e);
        }
        path = path.replace("\\", "/");
        path = path.replace(clazz.getPackage().getName().replace(".", "/"), "");
        path = path.replaceAll("(.*)/.*\\.class", "$1");
        return path;
    }
}
