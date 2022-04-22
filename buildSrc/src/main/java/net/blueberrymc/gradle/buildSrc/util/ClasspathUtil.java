package net.blueberrymc.gradle.buildSrc.util;

import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.Locale;

public class ClasspathUtil {
    public static @NotNull String getClasspath(@NotNull Class<?> clazz) {
        ProtectionDomain pd = clazz.getProtectionDomain();
        if (pd == null) throw new RuntimeException("ProtectionDomain of " + clazz.getTypeName() + " is null");
        String path;
        try {
            path = pd.getCodeSource().getLocation().toURI().getPath();
            if (path == null) {
                path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
                if (path.startsWith("file:")) path = path.substring(5);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (path.matches("^(.*\\.jar)!.*$")) path = path.replaceAll("^(.*\\.jar)!.*$", "$1");
        path = path.replace("\\", "/");
        if (!path.endsWith(".jar")) {
            if (clazz.getPackage() != null) {
                path = path.replace(clazz.getPackage().getName().replace(".", "/"), "");
                path = path.replaceAll("(.*)/.*\\.class", "$1");
            } else {
                path = path.replace(clazz.getTypeName().replace(".", "/") + ".class", "");
            }
        }
        if (path.endsWith("/") || path.endsWith("\\")) path = path.substring(0, path.length() - 1);
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") && path.matches("^/[A-Z]:/.*$")) {
            path = path.substring(1).replace("/", "\\");
        }
        return path;
    }
}
