package net.blueberrymc.common.bml.tools;

import com.sun.tools.javac.Main;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * "This is NOT part of any supported API.
 * If you write code that depends on this, you do so at your own
 * risk.  This code and its internal interfaces are subject to change
 * or deletion without notice."
 */
public final class JavaTools {
    public static final String TOOLS_JAR_PATH;
    public static final Throwable UNAVAILABLE_REASON;

    static {
        String path = System.getProperty("net.blueberry.common.bml.tools.path");
        File file;
        if (path == null || !new File(path).exists()) {
            String home = System.getProperty("java.home");
            file = new File(path = home + "/../lib/tools.jar");
        } else {
            file = new File(path);
        }
        TOOLS_JAR_PATH = path;
        UNAVAILABLE_REASON = load(file);
    }

    public static boolean isLoaded() {
        if (UNAVAILABLE_REASON != null) return false;
        try {
            Main.class.getClassLoader();
            return true;
        } catch (NoClassDefFoundError ex) {
            return false;
        }
    }

    @Nullable
    private static Throwable load(File file) {
        if (isLoaded()) return null; // it could happen, by running from an IDE.
        if (!file.exists()) return new UnsupportedOperationException("tools.jar is not available");
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            classLoader.loadClass("com.sun.tools.javac.Main");
            if (!isLoaded()) return new UnsupportedOperationException("Loaded tools.jar but could not load classes");
            return null;
        } catch (Throwable e) {
            return e;
        }
    }
}
