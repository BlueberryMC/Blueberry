package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class Ref {
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <S> RefClass<S> getClass(@NotNull Class<S> clazz) { return new RefClass<>(clazz); }

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Contract(value = "_ -> new", pure = true)
    public static <R> RefClass<R> getClassUnchecked(@NotNull Class<?> clazz) { return new RefClass<>((Class)clazz); }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <C> RefClass<C> forName(@NotNull String clazz) { return RefClass.forName(clazz); }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <C> RefClass<C> forName(@NotNull String clazz, boolean initialize, @Nullable ClassLoader classLoader) { return RefClass.forName(clazz, initialize, classLoader); }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <S> RefConstructor<S> getConstructor(@NotNull Class<S> clazz, @Nullable Class<?>... classes) {
        try {
            return new RefConstructor<>(clazz.getConstructor(classes));
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <S> RefConstructor<S> getDeclaredConstructor(@NotNull Class<S> clazz, @Nullable Class<?>... classes) {
        try {
            return new RefConstructor<>(clazz.getDeclaredConstructor(classes));
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <S> RefConstructor<S>[] getDeclaredConstructors(@NotNull Class<S> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors()).map(RefConstructor::new).toArray(RefConstructor[]::new);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <S> RefConstructor<S>[] getConstructors(@NotNull Class<S> clazz) {
        return Arrays.stream(clazz.getConstructors()).map(RefConstructor::new).toArray(RefConstructor[]::new);
    }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <S> RefMethod<S> getMethod(@NotNull Class<S> clazz, @NotNull String methodName, @Nullable Class<?>... classes) {
        try {
            return new RefMethod<>(clazz.getMethod(methodName, classes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <S> Optional<RefMethod<S>> getMethodOptional(@NotNull Class<S> clazz, @NotNull String methodName, @Nullable Class<?>... classes) {
        try {
            return Optional.of(new RefMethod<>(clazz.getMethod(methodName, classes)));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <S> RefMethod<S>[] getMethods(@NotNull Class<S> clazz) {
        return Arrays.stream(clazz.getMethods()).map(RefMethod::new).toArray(RefMethod[]::new);
    }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <S> RefMethod<S> getDeclaredMethod(@NotNull Class<S> clazz, @NotNull String methodName, @Nullable Class<?>... classes) {
        try {
            return new RefMethod<>(clazz.getDeclaredMethod(methodName, classes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <S> Optional<RefMethod<S>> getDeclaredMethodOptional(@NotNull Class<S> clazz, @NotNull String methodName, @Nullable Class<?>... classes) {
        try {
            return Optional.of(new RefMethod<>(clazz.getDeclaredMethod(methodName, classes)));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <S> RefMethod<S>[] getDeclaredMethods(@NotNull Class<S> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods()).map(RefMethod::new).toArray(RefMethod[]::new);
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <S> RefField<S> getField(@NotNull Class<S> clazz, @NotNull String fieldName) {
        try {
            return new RefField<>(clazz.getField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <S> RefField<S>[] getFields(@NotNull Class<S> clazz) {
        return Arrays.stream(clazz.getFields()).map(RefField::new).toArray(RefField[]::new);
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static <S> RefField<S> getDeclaredField(@NotNull Class<S> clazz, @NotNull String fieldName) {
        try {
            return new RefField<>(clazz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <S> RefField<S>[] getDeclaredFields(@NotNull Class<S> clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).map(RefField::new).toArray(RefField[]::new);
    }
}
