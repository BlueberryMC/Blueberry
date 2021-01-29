package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ModFile implements Closeable, AutoCloseable {
    private final JarFile jar;
    private final File dir;

    public ModFile(@NotNull("file") File file) throws IOException {
        Preconditions.checkNotNull(file, "file cannot be null");
        if (file.isFile()) {
            jar = new JarFile(file);
            dir = null;
        } else {
            jar = null;
            dir = file;
        }
    }

    @Nullable
    public ModFileEntry getEntry(String name) {
        JarEntry entry = getJarEntry(name);
        if (jar != null && entry != null) {
            try {
                return new ModFileEntry(jar.getInputStream(entry), entry.getCodeSigners());
            } catch (IOException ignore) {}
        }
        try {
            return new ModFileEntry(Objects.requireNonNull(getResourceAsStream(name)), null);
        } catch (IOException | NullPointerException ignore) {}
        return null;
    }

    @Nullable
    public JarEntry getJarEntry(String name) {
        return jar == null ? null : jar.getJarEntry(name);
    }

    @Nullable
    public Manifest getManifest() throws IOException {
        if (jar != null) return jar.getManifest();
        return null;
    }

    @Nullable
    public InputStream getResourceAsStream(@NotNull("name") String name) throws IOException {
        if (jar != null) {
            JarEntry entry = jar.getJarEntry(name);
            if (entry == null) return null;
            return jar.getInputStream(entry);
        }
        if (dir != null) {
            File file = new File(dir, name);
            if (!file.exists()) return null;
            return new FileInputStream(file);
        }
        throw new AssertionError();
    }

    @Override
    public void close() throws IOException {
        if (jar != null) jar.close();
    }
}
