package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    @Deprecated
    public Object invokeObj(Object obj, Object... args) {
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

    public Object invoke(T obj, Object... args) {
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
