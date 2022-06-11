package net.blueberrymc.common.internal.util;

import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.util.VarargFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ImplGetter {
    private static @NotNull Class<?> getImplClass(@NotNull Class<?> apiClass) throws ClassNotFoundException {
        String typeName = apiClass.getTypeName();
        if (!typeName.startsWith("net.blueberrymc.")) {
            throw new IllegalArgumentException("Cannot get impl class of " + typeName);
        }
        String implPackage = "net.blueberrymc.impl." + typeName.substring(16, typeName.lastIndexOf("."));
        String implClassName = typeName.substring(typeName.lastIndexOf(".") + 1);
        try {
            // net.blueberrymc.common.Test -> net.blueberrymc.impl.common.BlueberryTest
            return Class.forName(implPackage + ".Blueberry" + implClassName);
        } catch (ClassNotFoundException e) {
            try {
                String reallyClassName = implClassName;
                if (reallyClassName.contains("$")) {
                    reallyClassName = reallyClassName.substring(0, reallyClassName.indexOf("$"));
                }
                String dollar = implClassName.substring(implClassName.indexOf('$'));
                // net.blueberrymc.common.Test$AAAA -> net.blueberrymc.impl.common.TestImpl$AAAA
                return Class.forName(implPackage + "." + reallyClassName + "Impl" + dollar);
            } catch (ClassNotFoundException e1) {
                e1.addSuppressed(e);
                throw e1;
            }
        }
    }

    public static @NotNull VarargFunction<Object, Object> byMethod(@NotNull String name, @NotNull Class<?> @NotNull ... argumentTypes) {
        try {
            Class<?> caller = ReflectionHelper.getCallerClass();
            Method method = getImplClass(caller).getMethod(name, argumentTypes);
            return (args) -> {
                try {
                    return method.invoke(null, args);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull VarargFunction<Object, Object> byConstructor(@NotNull Class<?> @NotNull ... argumentTypes) {
        try {
            Class<?> caller = ReflectionHelper.getCallerClass();
            Constructor<?> method = getImplClass(caller).getConstructor(argumentTypes);
            return (args) -> {
                try {
                    return method.newInstance(args);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Contract(pure = true)
    public static Object field(@NotNull String name) {
        try {
            Class<?> caller = ReflectionHelper.getCallerClass();
            Field field = getImplClass(caller).getField(name);
            return field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Object getHandleOf(@NotNull Object o) {
        try {
            return o.getClass().getField("handle").get(o);
        } catch (NoSuchFieldException e) {
            try {
                return o.getClass().getMethod("getHandle").invoke(o);
            } catch (ReflectiveOperationException e1) {
                e1.addSuppressed(e);
                throw new RuntimeException(e1);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
