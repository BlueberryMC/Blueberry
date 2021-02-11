package net.blueberrymc.common.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceLocator {
    private static final ConcurrentHashMap<Class<?>, ResourceLocator> classMap = new ConcurrentHashMap<>();
    private final Class<?> clazz;

    private ResourceLocator(@NotNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "class cannot be null");
        this.clazz = clazz;
    }

    @Nullable
    public InputStream getResourceAsStream(@NotNull String name) {
        Preconditions.checkNotNull(name, "name cannot be null");
        InputStream in = clazz.getClassLoader().getResourceAsStream(name);
        if (in == null) {
            in = clazz.getResourceAsStream(name);
        }
        if (in == null && name.startsWith("/")) {
            in = clazz.getClassLoader().getResourceAsStream("." + name);
        }
        if (in == null && name.startsWith("/")) {
            in = clazz.getResourceAsStream("." + name);
        }
        return in;
    }

    @Nullable
    public URL getResource(@NotNull String name) {
        Preconditions.checkNotNull(name, "name cannot be null");
        URL url = clazz.getClassLoader().getResource(name);
        if (url == null) {
            url = clazz.getResource(name);
        }
        if (url == null && name.startsWith("/")) {
            url = clazz.getClassLoader().getResource("." + name);
        }
        if (url == null && name.startsWith("/")) {
            url = clazz.getResource("." + name);
        }
        return url;
    }

    @NotNull
    public static ResourceLocator getInstance(@NotNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "class cannot be null");
        if (!classMap.containsKey(clazz)) {
            classMap.put(clazz, new ResourceLocator(clazz));
        }
        return classMap.get(clazz);
    }
}
