package net.blueberrymc.util;

import com.mojang.datafixers.DataFixUtils;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.util.VoidSafeExecutor;
import net.blueberrymc.common.util.function.ThrowableSupplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Util {
    @Contract(value = "null, null -> null; null, _ -> param2; _, !null -> !null", pure = true)
    public static <T> T getOrDefault(@Nullable T value, @Nullable T def) {
        if (value != null) return value;
        return def;
    }

    @SafeVarargs
    @Contract(value = "null -> null", pure = true)
    public static <T> T getOrDefault(@Nullable T@Nullable... values) {
        if (values == null) return null;
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }

    @Contract(pure = true)
    public static <V, R> R mapOrGet(@Nullable V value, @NotNull Function<V, R> mapper, @NotNull Supplier<R> defaultValueSupplier) {
        if (value != null) return mapper.apply(value);
        return defaultValueSupplier.get();
    }

    @Contract(pure = true)
    public static <V, R> R mapOrElse(@Nullable V value, @NotNull Function<V, R> mapper, @Nullable R def) {
        if (value != null) return mapper.apply(value);
        return def;
    }

    @NotNull
    public static Thread createThread(@NotNull String name, boolean daemon, @NotNull Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(name);
        thread.setDaemon(daemon);
        return thread;
    }

    @NotNull
    public static Thread createThread(@NotNull String name, @NotNull Runnable runnable) {
        return createThread(name, false, runnable);
    }

    @Contract(pure = true)
    public static <T> T waitUntilReturns(@NotNull String threadName, @NotNull Supplier<T> supplier) throws RuntimeException {
        return waitUntilReturns(threadName, false, supplier);
    }

    @Contract(pure = true)
    public static <T> T waitUntilReturns(@NotNull String threadName, boolean pollEvents, @NotNull Supplier<T> supplier) throws RuntimeException {
        AtomicBoolean done = new AtomicBoolean(false);
        AtomicReference<T> value = new AtomicReference<>();
        AtomicReference<RuntimeException> exception = new AtomicReference<>();
        createThread(threadName, () -> {
            try {
                value.set(supplier.get());
            } catch (RuntimeException e) {
                exception.set(e);
            } finally {
                done.set(true);
                synchronized (done) {
                    done.notifyAll();
                }
            }
        }).start();
        while (!done.get()) {
            if (pollEvents) {
                Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                    @Override
                    public void execute() {
                        org.lwjgl.glfw.GLFW.glfwPollEvents();
                    }
                });
            }
            synchronized (done) {
                try {
                    done.wait(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (exception.get() != null) throw exception.get();
        return value.get();
    }

    public static byte clamp(byte value, byte min, byte max) {
        if (value < min) {
            return min;
        }
        return (value <= max) ? value : max;
    }

    public static short clamp(short value, short min, short max) {
        if (value < min) {
            return min;
        }
        return (value <= max) ? value : max;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static long clamp(long value, long min, long max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    @NotNull
    public static String getSimpleName(@NotNull Class<?> clazz) {
        String name = clazz.getSimpleName();
        if (name.length() > 0) return name;
        Class<?> superClass = clazz.getSuperclass();
        while (name.length() == 0 && superClass != null) {
            name = superClass.getSimpleName();
            superClass = superClass.getSuperclass();
        }
        while (name.length() == 0) {
            for (Class<?> c : clazz.getInterfaces()) {
                name = getSimpleName(c);
                if (name.length() > 0) return name;
            }
        }
        return name;
    }

    private static final Map<Class<?>, String> extendedSimpleNameCache = new HashMap<>();

    @NotNull
    public static String getExtendedSimpleName(@NotNull Class<?> clazz) {
        if (extendedSimpleNameCache.containsKey(clazz)) return extendedSimpleNameCache.get(clazz);
        String simpleName = getSimpleName(clazz);
        Class<?> sc = clazz.getSuperclass();
        if (sc != null && sc != Object.class) {
            String superName = simpleName;
            while (superName.equals(simpleName) && sc != null) {
                superName = getSimpleName(sc);
                sc = sc.getSuperclass();
            }
            if (superName.equals("Object")) {
                extendedSimpleNameCache.put(clazz, simpleName);
                return simpleName;
            }
            String name = simpleName + " extends " + superName;
            extendedSimpleNameCache.put(clazz, name);
            return name;
        }
        extendedSimpleNameCache.put(clazz, simpleName);
        return simpleName;
    }

    @NotNull
    public static String capitalize(@NotNull String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    @Nullable
    public static <T> T parseArgument(@NotNull OptionSet optionSet, @NotNull OptionSpec<T> optionSpec) {
        try {
            return optionSet.valueOf(optionSpec);
        } catch (Throwable throwable) {
            if (optionSpec instanceof ArgumentAcceptingOptionSpec<T> argumentAcceptingOptionSpec) {
                List<T> defaults = argumentAcceptingOptionSpec.defaultValues();
                if (!defaults.isEmpty()) {
                    return defaults.get(0);
                }
            }
            throw throwable;
        }
    }

    @NotNull
    public static <T> Stream<T> toStream(@NotNull @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<? extends T> optional) {
        return DataFixUtils.orElseGet(optional.map(Stream::of), Stream::empty);
    }

    @Nullable
    public static Package getPackageRecursively(@NotNull ClassLoader cl, @NotNull String name) {
        Package pkg = cl.getDefinedPackage(name);
        if (pkg == null) {
            ClassLoader parent = cl.getParent();
            while (pkg == null && parent != null) {
                pkg = parent.getDefinedPackage(name);
                parent = parent.getParent();
            }
            if (pkg == null) pkg = ClassLoader.getSystemClassLoader().getDefinedPackage(name);
            if (pkg == null) pkg = ClassLoader.getPlatformClassLoader().getDefinedPackage(name);
        }
        return pkg;
    }

    @NotNull
    @Contract(pure = true)
    public static <T> T required(@NotNull ThrowableSupplier<T> supplier) {
        try {
            T value = supplier.get();
            if (value == null) throw new IllegalStateException("Supplier returned null");
            return value;
        } catch (Exception throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
