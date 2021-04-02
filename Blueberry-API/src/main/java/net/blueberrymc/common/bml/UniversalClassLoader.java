package net.blueberrymc.common.bml;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class UniversalClassLoader extends URLClassLoader {
    public UniversalClassLoader(@NotNull URL[] urls, @NotNull ClassLoader parent) {
        super(urls, parent);
    }

    public UniversalClassLoader(@NotNull URL[] urls) {
        super(urls);
    }

    public UniversalClassLoader(@NotNull URL[] urls, @NotNull ClassLoader parent, @NotNull URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    public void addURL(@NotNull URL url) {
        super.addURL(url);
    }
}
