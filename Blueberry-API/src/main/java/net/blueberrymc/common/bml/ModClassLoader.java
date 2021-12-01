package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.config.ModDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;

public class ModClassLoader extends URLClassLoader {
    protected final BlueberryModLoader modLoader;
    protected final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    protected final ModDescriptionFile description;
    protected final File file;
    protected final ModFile modFile;
    protected final Manifest manifest;
    protected final BlueberryMod mod;
    protected final URL url;
    protected BlueberryMod initializedMod = null;
    protected Throwable state;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public ModClassLoader(@NotNull BlueberryModLoader modLoader, @NotNull ClassLoader parent, @NotNull ModDescriptionFile description, @NotNull File file) throws IOException {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.file = file;
        this.modLoader = modLoader;
        this.modFile = new ModFile(file);
        this.manifest = modFile.getManifest();
        this.description = description;
        this.url = file.toURI().toURL();
        try {
            Class<?> mainClass;
            try {
                mainClass = this.findClass(description.getMainClass());
            } catch (ClassNotFoundException ex) {
                throw new InvalidModException("Cannot find main class '" + description.getMainClass() + "' of mod '" + description.getModId() + "'", ex);
            }
            Class<? extends BlueberryMod> modClass;
            try {
                modClass = mainClass.asSubclass(BlueberryMod.class);
            } catch (ClassCastException ex) {
                throw new InvalidModException("Main class '" + description.getMainClass() + "' of mod '" + description.getModId() + "' does not extend BlueberryMod");
            }
            mod = modClass.getDeclaredConstructor().newInstance();
        } catch (IllegalAccessException | NoSuchMethodException ex) {
            throw new InvalidModException("No public constructor", ex);
        } catch (InstantiationException ex) {
            throw new InvalidModException("Illegal main class type", ex);
        } catch (InvocationTargetException ex) {
            throw new InvalidModException("Constructor threw exception", ex.getCause());
        }
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }

    @NotNull
    public BlueberryMod getMod() {
        return mod;
    }

    @NotNull
    public BlueberryModLoader getModLoader() {
        return modLoader;
    }

    @NotNull
    public File getFile() {
        return file;
    }

    @Nullable
    @Override
    public URL getResource(@NotNull String name) {
        return findResource(name);
    }

    @NotNull
    @Override
    public Enumeration<URL> getResources(@NotNull String name) throws IOException {
        return findResources(name);
    }

    @NotNull
    @Override
    protected Class<?> findClass(@NotNull String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    @NotNull
    protected Class<?> findClass(@NotNull String name, boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if (result != null) return result;
        if (checkGlobal) {
            result = modLoader.findClass(name);
        }
        if (result == null) {
            String path = name.replace(".", "/").concat(".class");
            ModFileEntry entry = this.modFile.getEntry(path);
            if (entry != null) {
                byte[] bytes;
                try (InputStream in = entry.getInputStream()) {
                    bytes = ByteStreams.toByteArray(in);
                } catch (IOException ex) {
                    throw new ClassNotFoundException(name, ex);
                }
                bytes = Blueberry.getUtil().processClass(name, bytes);
                int dot = name.lastIndexOf('.');
                if (dot != -1) {
                    String pkgName = name.substring(0, dot);
                    if (getPackageRecursively(pkgName) == null) {
                        try {
                            if (manifest != null) {
                                definePackage(pkgName, manifest, url);
                            } else {
                                definePackage(pkgName, null, null, null, null, null, null, null);
                            }
                        } catch (IllegalArgumentException ex) {
                            if (getPackageRecursively(pkgName) == null) {
                                throw new IllegalStateException("Cannot find package " + pkgName);
                            }
                        }
                    }
                }
                CodeSigner[] signers = entry.getCodeSigners();
                CodeSource source = new CodeSource(url, signers);
                result = defineClass(name, bytes, 0, bytes.length, source);
            }
            if (result == null) {
                result = super.findClass(name);
            }
            if (result != null) {
                modLoader.setClass(name, result);
            }
            classes.put(name, result);
        }
        if (result == null) throw new ClassNotFoundException(name);
        return result;
    }

    @Nullable
    protected Package getPackageRecursively(@NotNull String name) {
        Package pkg = getDefinedPackage(name);
        if (pkg == null) {
            ClassLoader parent = getParent();
            while (pkg == null && parent != null) {
                pkg = parent.getDefinedPackage(name);
                parent = parent.getParent();
            }
            if (pkg == null) pkg = ClassLoader.getSystemClassLoader().getDefinedPackage(name);
            if (pkg == null) pkg = ClassLoader.getPlatformClassLoader().getDefinedPackage(name);
        }
        return pkg;
    }

    public boolean isClosed() {
        return modFile.isClosed();
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            modFile.close();
        }
    }

    @NotNull
    public Map<String, Class<?>> getClasses() { return classes; }

    protected synchronized void initialize(@NotNull BlueberryMod mod) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        if (mod.getClass().getClassLoader() != this)
            throw new IllegalArgumentException("Cannot initialize mod outside of this class loader");
        if (this.mod != null && this.initializedMod != null)
            throw new IllegalArgumentException("Cannot reinitialize mod: " + description.getModId(), state);
        state = new IllegalStateException("Initialization: " + description.getModId());
        this.initializedMod = mod;
        mod.getStateList().add(ModState.LOADED);
        mod.init(modLoader, description, this, this.file);
    }
}
