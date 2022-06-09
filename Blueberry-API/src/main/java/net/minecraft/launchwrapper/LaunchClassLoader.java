package net.minecraft.launchwrapper;

import net.blueberrymc.common.bml.ModClassLoader;
import net.blueberrymc.nativeutil.NativeUtil;
import net.blueberrymc.server.main.ServerMain;
import net.blueberrymc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class LaunchClassLoader extends URLClassLoader {
    private static final Logger LOGGER = LogManager.getLogger("LaunchWrapper");
    public static final int BUFFER_SIZE = 1 << 12;
    private final List<URL> sources;
    private final ClassLoader parent = getClass().getClassLoader();

    private final List<IClassTransformer> transformers = new ArrayList<>(2);
    private final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    private final Set<String> invalidClasses = new HashSet<>(1000);

    private final Set<String> classLoaderExceptions = new HashSet<>();
    private final Set<String> transformerExceptions = new HashSet<>();
    private final Map<String,byte[]> resourceCache = new ConcurrentHashMap<>(1000);
    private final Set<String> negativeResourceCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private IClassNameTransformer renameTransformer;

    private final ThreadLocal<byte[]> loadBuffer = new ThreadLocal<>();

    private static final String[] RESERVED_NAMES = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("legacy.debugClassLoading", "false"));
    private static final boolean DEBUG_FINER = DEBUG && Boolean.parseBoolean(System.getProperty("legacy.debugClassLoadingFiner", "false"));
    private static final boolean DEBUG_SAVE = DEBUG && Boolean.parseBoolean(System.getProperty("legacy.debugClassLoadingSave", "false"));
    private static File tempFolder = null;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public LaunchClassLoader(@NotNull URL@NotNull[] sources, @Nullable ClassLoader parent) {
        super(sources, parent);
        this.sources = new ArrayList<>(Arrays.asList(sources));

        // classloader exclusions
        addClassLoaderExclusion("java.");
        addClassLoaderExclusion("com.sun.");
        addClassLoaderExclusion("sun.");
        addClassLoaderExclusion("org.lwjgl.");
        addClassLoaderExclusion("org.apache.logging.");
        addClassLoaderExclusion("net.minecraft.launchwrapper.");
        addClassLoaderExclusion("net.blueberrymc.client.main.ClientMain");
        addClassLoaderExclusion("net.blueberrymc.server.main.ServerMain");
        addClassLoaderExclusion("com.google.gson.");
        addClassLoaderExclusion("com.google.common.");
        addClassLoaderExclusion("com.mojang.bridge.");
        addClassLoaderExclusion("io.netty.");
        addClassLoaderExclusion("it.unimi.dsi.fastutil.");
        addClassLoaderExclusion("org.slf4j.");
        addClassLoaderExclusion("org.objectweb.asm.");
        addClassLoaderExclusion("jdk.");

        // transformer exclusions
        addTransformerExclusion("javax.");
        addTransformerExclusion("argo.");
        addTransformerExclusion("org.objectweb.asm.");
        addTransformerExclusion("com.google.common.");
        addTransformerExclusion("org.bouncycastle.");
        addTransformerExclusion("net.minecraft.launchwrapper.injector.");

        if (DEBUG_SAVE) {
            int x = 1;
            tempFolder = new File(Launch.minecraftHome, "CLASSLOADER_TEMP");
            while (tempFolder.exists() && x <= 10) {
                tempFolder = new File(Launch.minecraftHome, "CLASSLOADER_TEMP" + x++);
            }

            if (tempFolder.exists()) {
                LOGGER.info("DEBUG_SAVE enabled, but 10 temp directories already exist, clean them and try again.");
                tempFolder = null;
            } else {
                LOGGER.info("DEBUG_SAVE Enabled, saving all classes to \"{}\"", tempFolder.getAbsolutePath().replace('\\', '/'));
                tempFolder.mkdirs();
            }
        }
    }

    public void registerTransformer(@NotNull String transformerClassName) {
        try {
            IClassTransformer transformer = (IClassTransformer) loadClass(transformerClassName).getDeclaredConstructor().newInstance();
            transformers.add(transformer);
            if (transformer instanceof IClassNameTransformer && renameTransformer == null) {
                renameTransformer = (IClassNameTransformer) transformer;
            }
        } catch (Exception e) {
            LOGGER.error("A critical problem occurred registering the ASM transformer class {}", transformerClassName, e);
        }
    }

    @NotNull
    @Override
    protected Class<?> loadClass(@NotNull String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = this.findLoadedClass(name);
        if (clazz != null) return clazz;
        try {
            return this.findClass(name);
        } catch (ClassNotFoundException ignore) {}
        clazz = parent.loadClass(name);
        if (clazz != null) return clazz;
        throw new ClassNotFoundException(name);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Set<ClassLoader> getLoaders() {
        Object o = ServerMain.blackboard.get("bml");
        if (o != null) {
            try {
                return (Set<ClassLoader>) NativeUtil.get(o.getClass().getDeclaredField("loaders"), o);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Nullable
    public Class<?> findClassFromLoaders(@NotNull String name) {
        Set<ClassLoader> loaders = getLoaders();
        if (loaders == null) return null;
        Class<?> result = null;
        for (ClassLoader loader : loaders) {
            try {
                if (loader instanceof ModClassLoader mcl) {
                    result = mcl.findClass(name, false);
                } else {
                    try {
                        Method m = loader.getClass().getMethod("findClass", String.class, boolean.class);
                        result = (Class<?>) m.invoke(loader, name, false);
                    } catch (NoSuchMethodException | IllegalAccessException ignore) {
                    } catch (InvocationTargetException ignore) {
                        // if (e.getTargetException() instanceof ClassNotFoundException)
                        continue;
                    }
                    result = loader.loadClass(name);
                }
            } catch (ClassNotFoundException ignore) {}
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    @NotNull
    public Class<?> findClass(@NotNull final String name) throws ClassNotFoundException {
        if (!ModClassLoader.shouldUseLaunchClassLoader(name)) {
            Class<?> clazz = findClassFromLoaders(name);
            if (clazz != null) return clazz;
        }

        if (invalidClasses.contains(name)) {
            throw new ClassNotFoundException(name);
        }

        for (final String exception : classLoaderExceptions) {
            if (name.startsWith(exception)) {
                return parent.loadClass(name);
            }
        }

        if (cachedClasses.containsKey(name)) {
            return cachedClasses.get(name);
        }

        for (final String exception : transformerExceptions) {
            if (name.startsWith(exception)) {
                try {
                    final Class<?> clazz = super.findClass(name);
                    cachedClasses.put(name, clazz);
                    return clazz;
                } catch (ClassNotFoundException e) {
                    invalidClasses.add(name);
                    throw e;
                }
            }
        }

        try {
            final String transformedName = transformName(name);
            if (cachedClasses.containsKey(transformedName)) {
                return cachedClasses.get(transformedName);
            }

            final String untransformedName = untransformName(name);

            final int lastDot = untransformedName.lastIndexOf('.');
            final String packageName = lastDot == -1 ? "" : untransformedName.substring(0, lastDot);
            final String fileName = untransformedName.replace('.', '/').concat(".class");
            URLConnection urlConnection = findCodeSourceConnectionFor(fileName);

            CodeSigner[] signers = null;

            if (lastDot > -1 && !untransformedName.startsWith("net.minecraft.")) {
                if (urlConnection instanceof JarURLConnection jarURLConnection) {
                    final JarFile jarFile = jarURLConnection.getJarFile();

                    if (jarFile != null && jarFile.getManifest() != null) {
                        final Manifest manifest = jarFile.getManifest();
                        final JarEntry entry = jarFile.getJarEntry(fileName);

                        Package pkg = Util.getPackageRecursively(this, packageName);
                        getClassBytes(untransformedName);
                        signers = entry.getCodeSigners();
                        if (pkg == null) {
                            pkg = definePackage(packageName, manifest, jarURLConnection.getJarFileURL());
                        }
                        if (pkg.isSealed() && !pkg.isSealed(jarURLConnection.getJarFileURL())) {
                            LOGGER.error("The jar file {} is trying to seal already secured path {}", jarFile.getName(), packageName);
                        } else if (isSealed(packageName, manifest)) {
                            LOGGER.error("The jar file {} has a security seal for path {}, but that path is defined and not secure", jarFile.getName(), packageName);
                        }
                    }
                } else {
                    Package pkg = Util.getPackageRecursively(this, packageName);
                    if (pkg == null) {
                        pkg = definePackage(packageName, null, null, null, null, null, null, null);
                    }
                    if (pkg.isSealed()) {
                        LOGGER.error("The URL {} is defining elements for sealed path {}", Objects.requireNonNull(urlConnection).getURL(), packageName);
                    }
                }
            }

            final byte[] transformedClass = runTransformers(untransformedName, transformedName, getClassBytes(untransformedName));
            if (DEBUG_SAVE) {
                saveTransformedClass(transformedClass, transformedName);
            }

            final CodeSource codeSource = urlConnection == null ? null : new CodeSource(urlConnection.getURL(), signers);
            final Class<?> clazz = defineClass(transformedName, transformedClass, 0, transformedClass.length, codeSource);
            cachedClasses.put(transformedName, clazz);
            return clazz;
        } catch (Throwable e) {
            invalidClasses.add(name);
            if (DEBUG) {
                LOGGER.error("Exception encountered attempting classloading of {}", name, e);
            }
            // for some reason, instanceof returns false, so we do this instead
            if (e.getClass().getTypeName().equals("net.blueberrymc.common.util.BlueberryEvil$WrongSideException")) {
                throw (RuntimeException) e;
            }
            throw new ClassNotFoundException(name, e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveTransformedClass(byte@NotNull[] data, @NotNull String transformedName) {
        if (tempFolder == null) {
            return;
        }

        final File outFile = new File(tempFolder, transformedName.replace('.', File.separatorChar) + ".class");
        final File outDir = outFile.getParentFile();

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        if (outFile.exists()) {
            outFile.delete();
        }

        try {
            LOGGER.info("Saving transformed class \"{}\" to \"{}\"", transformedName, outFile.getAbsolutePath().replace('\\', '/'));

            final OutputStream output = new FileOutputStream(outFile);
            output.write(data);
            output.close();
        } catch (IOException ex) {
            LOGGER.warn("Could not save transformed class \"{}\"", transformedName);
        }
    }

    private String untransformName(@NotNull final String name) {
        if (renameTransformer != null) {
            return renameTransformer.unmapClassName(name);
        }

        return name;
    }

    private String transformName(@NotNull final String name) {
        if (renameTransformer != null) {
            return renameTransformer.remapClassName(name);
        }

        return name;
    }

    private boolean isSealed(@NotNull final String path, @NotNull final Manifest manifest) {
        Attributes attributes = manifest.getAttributes(path);
        String sealed = null;
        if (attributes != null) {
            sealed = attributes.getValue(Name.SEALED);
        }

        if (sealed == null) {
            attributes = manifest.getMainAttributes();
            if (attributes != null) {
                sealed = attributes.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }

    @Nullable
    private URLConnection findCodeSourceConnectionFor(@NotNull final String name) {
        final URL resource = findResource(name);
        if (resource != null) {
            try {
                return resource.openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    private byte[] runTransformers(final String name, final String transformedName, byte[] basicClass) {
        if (DEBUG_FINER) {
            LOGGER.info("Beginning transform of {{} ({})} Start Length: {}", name, transformedName, (basicClass == null ? 0 : basicClass.length));
            for (final IClassTransformer transformer : transformers) {
                final String transName = transformer.getClass().getName();
                LOGGER.info("Before Transformer {{} ({})} {}: {}", name, transformedName, transName, (basicClass == null ? 0 : basicClass.length));
                basicClass = transformer.transform(name, transformedName, basicClass);
                LOGGER.info("After  Transformer {{} ({})} {}: {}", name, transformedName, transName, (basicClass == null ? 0 : basicClass.length));
            }
            LOGGER.debug("Ending transform of {{} ({})} Start Length: {}", name, transformedName, (basicClass == null ? 0 : basicClass.length));
        } else {
            for (final IClassTransformer transformer : transformers) {
                basicClass = transformer.transform(name, transformedName, basicClass);
            }
        }
        return basicClass;
    }

    @Override
    public void addURL(@NotNull final URL url) {
        super.addURL(url);
        sources.add(url);
    }

    @NotNull
    public List<URL> getSources() {
        return sources;
    }

    private byte[] readFully(InputStream stream) {
        try {
            byte[] buffer = getOrCreateBuffer();

            int read;
            int totalLength = 0;
            while ((read = stream.read(buffer, totalLength, buffer.length - totalLength)) != -1) {
                totalLength += read;

                // Extend our buffer
                if (totalLength >= buffer.length - 1) {
                    byte[] newBuffer = new byte[buffer.length + BUFFER_SIZE];
                    System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                    buffer = newBuffer;
                }
            }

            final byte[] result = new byte[totalLength];
            System.arraycopy(buffer, 0, result, 0, totalLength);
            return result;
        } catch (Throwable t) {
            LOGGER.warn("Problem loading class", t);
            return new byte[0];
        }
    }

    private byte[] getOrCreateBuffer() {
        byte[] buffer = loadBuffer.get();
        if (buffer == null) {
            loadBuffer.set(new byte[BUFFER_SIZE]);
            buffer = loadBuffer.get();
        }
        return buffer;
    }

    @NotNull
    public List<IClassTransformer> getTransformers() {
        return Collections.unmodifiableList(transformers);
    }

    public void addClassLoaderExclusion(@NotNull String toExclude) {
        classLoaderExceptions.add(toExclude);
    }

    public void addTransformerExclusion(@NotNull String toExclude) {
        transformerExceptions.add(toExclude);
    }

    public byte@Nullable[] getClassBytes(@NotNull String name) throws IOException {
        if (negativeResourceCache.contains(name)) {
            return null;
        } else if (resourceCache.containsKey(name)) {
            return resourceCache.get(name);
        }
        if (name.indexOf('.') == -1) {
            for (final String reservedName : RESERVED_NAMES) {
                if (name.toUpperCase(Locale.ENGLISH).startsWith(reservedName)) {
                    final byte[] data = getClassBytes("_" + name);
                    if (data != null) {
                        resourceCache.put(name, data);
                        return data;
                    }
                }
            }
        }

        InputStream classStream = null;
        try {
            final String resourcePath = name.replace('.', '/').concat(".class");
            final URL classResource = findResource(resourcePath);

            if (classResource == null) {
                if (DEBUG) LOGGER.info("Failed to find class resource {}", resourcePath);
                negativeResourceCache.add(name);
                return null;
            }
            classStream = classResource.openStream();

            if (DEBUG) LOGGER.info("Loading class {} from resource {}", name, classResource.toString());
            final byte[] data = readFully(classStream);
            resourceCache.put(name, data);
            return data;
        } finally {
            closeSilently(classStream);
        }
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void clearNegativeEntries(@NotNull Set<String> entriesToClear) {
        negativeResourceCache.removeAll(entriesToClear);
    }

    @Nullable
    @Override
    public InputStream getResourceAsStream(@NotNull String name) {
        InputStream in = super.getResourceAsStream(name);
        if (in == null) in = parent.getResourceAsStream(name);
        if (in != null) return in;
        Object bml = ServerMain.blackboard.get("bml");
        if (bml == null) return null;
        try {
            Method m = bml.getClass().getMethod("getResourceAsStream", String.class);
            return (InputStream) m.invoke(bml, name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
}
