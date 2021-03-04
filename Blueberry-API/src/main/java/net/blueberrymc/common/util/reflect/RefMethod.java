package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class RefMethod<T> extends RefExecutable {
    @NotNull
    private final Method method;

    @NotNull
    public Method getMethod() { return method; }

    /**
     * @deprecated obj is unchecked, may throw exception at runtime
     * @param obj the object (unchecked type)
     * @param args invoke arguments
     * @return the return value
     */
    @Contract
    @Deprecated
    public Object invokeObj(@Nullable Object obj, @Nullable Object@Nullable... args) {
        try {
            return this.method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public RefMethod(@NotNull Method method) {
        super(method);
        this.method = method;
    }

    @Contract
    public Object invoke(@Nullable T obj, @Nullable Object@Nullable... args) {
        try {
            return this.method.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    public boolean isBridge() { return this.method.isBridge(); }

    @Override
    @NotNull
    @Contract(pure = true)
    public String toString() {
        return this.method.toString();
    }

    @Contract("_ -> this")
    @NotNull
    public RefMethod<T> accessible(boolean flag) { setAccessible(flag); return this; }

    @Override
    public @NotNull Member getMember() { return method; }
}
