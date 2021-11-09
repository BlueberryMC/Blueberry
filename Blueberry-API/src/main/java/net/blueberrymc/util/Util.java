package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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
            if (pollEvents) GLFW.glfwPollEvents();
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
}
