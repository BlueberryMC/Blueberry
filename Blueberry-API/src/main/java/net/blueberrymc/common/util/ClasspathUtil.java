package net.blueberrymc.common.util;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.InvalidModException;
import net.blueberrymc.util.OSType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ClasspathUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static @NotNull URL pathToURL(@NotNull String path) throws MalformedURLException {
        MalformedURLException ex;
        try {
            return new File(path).toURI().toURL();
        } catch (MalformedURLException exception) {
            ex = exception;
        }
        path = "file:" + path;
        try {
            return new URL(path);
        } catch (MalformedURLException exception) {
            exception.addSuppressed(ex);
            throw exception;
        }
    }

    public static @NotNull URL getClasspathAsURL(@NotNull Class<?> clazz) throws MalformedURLException {
        return pathToURL(getClasspath(clazz));
    }

    public static @NotNull String getClasspath(@NotNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "class cannot be null");
        String path;
        try {
            path = clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if (path == null) {
                path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
                if (path.startsWith("file:")) path = path.substring(5);
            }
        } catch (URISyntaxException e) {
            throw new InvalidModException(e);
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
        if (OSType.detectOS() == OSType.Windows && path.matches("^/[A-Z]:/.*$")) {
            path = path.substring(1).replace("/", "\\");
        }
        return path;
    }
}
