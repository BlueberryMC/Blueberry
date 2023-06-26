package net.blueberrymc.common.util.reflect;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.common.util.ThrowableActionableResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public record RefClass<T>(@NotNull Class<T> clazz) {
    @SuppressWarnings("unchecked")
    @NotNull
    public static <C> RefClass<C> forName(@NotNull String clazz) {
        Preconditions.checkNotNull(clazz, "clazz cannot be null");
        if (clazz.equals("int")) return new RefClass<>((Class<C>) Integer.TYPE);
        if (clazz.equals("void")) return new RefClass<>((Class<C>) Void.TYPE);
        if (clazz.equals("long")) return new RefClass<>((Class<C>) Long.TYPE);
        if (clazz.equals("boolean")) return new RefClass<>((Class<C>) Boolean.TYPE);
        if (clazz.equals("float")) return new RefClass<>((Class<C>) Float.TYPE);
        if (clazz.equals("double")) return new RefClass<>((Class<C>) Double.TYPE);
        if (clazz.equals("byte")) return new RefClass<>((Class<C>) Byte.TYPE);
        if (clazz.equals("short")) return new RefClass<>((Class<C>) Short.TYPE);
        try {
            return new RefClass<>((Class<C>) Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <C> RefClass<C> forName(@NotNull String clazz, boolean initialize, @Nullable ClassLoader classLoader) {
        try {
            return new RefClass<>((Class<C>) Class.forName(clazz, initialize, classLoader));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    @NotNull
    public RefClass<?> unchecked() {
        return Ref.getClassUnchecked(clazz);
    }

    @Contract(pure = true)
    @NotNull
    public RefField<T> getDeclaredField(@NotNull String fieldName) {
        return Ref.getDeclaredField(this.clazz, fieldName);
    }

    @Contract(pure = true)
    @NotNull
    public RefField<T> getField(@NotNull String fieldName) {
        return Ref.getField(this.clazz, fieldName);
    }

    @Contract(pure = true)
    @NotNull
    public RefConstructor<T> getConstructor(@NotNull Class<?> @NotNull ... classes) {
        return Ref.getConstructor(this.clazz, classes);
    }

    @Contract(pure = true)
    @NotNull
    public RefConstructor<T> getDeclaredConstructor(@NotNull Class<?> @NotNull ... classes) {
        return Ref.getDeclaredConstructor(this.clazz, classes);
    }

    @Contract(pure = true)
    @Nullable
    public RefConstructor<T> getDeclaredConstructorMaybe(@NotNull Class<?> @NotNull ... classes) {
        return ThrowableActionableResult.of(() -> getDeclaredConstructor(classes)).orElse(null);
    }

    @Contract(pure = true)
    @NotNull
    public RefConstructor<T> @NotNull [] getConstructors() {
        return Ref.getConstructors(this.clazz);
    }

    @Contract(pure = true)
    @NotNull
    public RefConstructor<T> @NotNull [] getDeclaredConstructors() {
        return Ref.getDeclaredConstructors(this.clazz);
    }

    @Contract(pure = true)
    @NotNull
    public RefMethod<T> getMethod(@NotNull String methodName, @NotNull Class<?> @NotNull ... classes) {
        return Ref.getMethod(this.clazz, methodName, classes);
    }

    @Contract(pure = true)
    @NotNull
    public RefMethod<T> getDeclaredMethod(@NotNull String methodName, @NotNull Class<?> @NotNull ... classes) {
        return Ref.getDeclaredMethod(this.clazz, methodName, classes);
    }

    @Contract(pure = true)
    @NotNull
    public RefMethod<T> @NotNull [] getMethods() {
        return Ref.getMethods(this.clazz);
    }

    @Contract(pure = true)
    @NotNull
    public RefMethod<T> @NotNull [] getDeclaredMethods() {
        return Ref.getDeclaredMethods(this.clazz);
    }

    @Contract(pure = true)
    @NotNull
    public RefField<T> @NotNull [] getFields() {
        return Ref.getFields(this.clazz);
    }

    @Contract(pure = true)
    @NotNull
    public RefField<T> @NotNull [] getDeclaredFields() {
        return Ref.getDeclaredFields(this.clazz);
    }

    @Contract(pure = true)
    @NotNull
    public <U> RefClass<? extends U> asSubClass(@NotNull Class<U> clazz) {
        return new RefClass<>(this.clazz.asSubclass(clazz));
    }

    @Nullable
    @Contract(pure = true)
    public <A extends Annotation> A getAnnotation(@NotNull Class<A> annotationClass) {
        return this.clazz.getAnnotation(annotationClass);
    }

    public boolean isAnnotationPresent(@NotNull Class<? extends Annotation> annotationClass) {
        return this.clazz.isAnnotationPresent(annotationClass);
    }

    @Contract(pure = true)
    @NotNull
    public <A extends Annotation> A @NotNull [] getAnnotationsByType(@NotNull Class<A> annotationClass) {
        return this.clazz.getAnnotationsByType(annotationClass);
    }

    @NotNull
    public Annotation[] getAnnotations() {
        return this.clazz.getAnnotations();
    }

    @Nullable
    public <A extends Annotation> A getDeclaredAnnotation(@NotNull Class<A> annotationClass) {
        return this.clazz.getDeclaredAnnotation(annotationClass);
    }

    @NotNull
    public <A extends Annotation> A @NotNull [] getDeclaredAnnotationsByType(@NotNull Class<A> annotationClass) {
        return this.clazz.getDeclaredAnnotationsByType(annotationClass);
    }

    @NotNull
    public Annotation @NotNull [] getDeclaredAnnotations() {
        return this.clazz.getDeclaredAnnotations();
    }

    @Contract(value = "_ -> param1", pure = true)
    public T cast(@NotNull Object obj) {
        return this.clazz.cast(obj);
    }

    @NotNull
    public T @NotNull [] getEnumConstants() {
        return this.clazz.getEnumConstants();
    }

    public boolean isExtends(@NotNull Class<?> clazz) {
        return ReflectionHelper.getSupers(this.clazz).contains(clazz);
    }

    @NotNull
    public T newInstance() {
        return this.getDeclaredConstructor().newInstance();
    }
}
