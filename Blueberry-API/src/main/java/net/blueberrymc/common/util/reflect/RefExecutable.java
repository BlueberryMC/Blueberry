package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.TypeVariable;

public class RefExecutable implements RefMember {
    private final Executable executable;

    public RefExecutable(@NotNull Executable executable) {
        this.executable = executable;
    }

    @NotNull
    public Executable getExecutable() { return executable; }

    @NotNull
    public Class<?> getDeclaringClass() { return executable.getDeclaringClass(); }

    @NotNull
    public String getName() { return executable.getName(); }

    public int getModifiers() { return executable.getModifiers(); }

    @NotNull
    public TypeVariable<?>@NotNull[] getTypeParameters() { return executable.getTypeParameters(); }

    @NotNull
    public Class<?>@NotNull[] getParameterTypes() { return executable.getParameterTypes(); }

    @NotNull
    public Class<?>@NotNull[] getExceptionTypes() { return executable.getExceptionTypes(); }

    @NotNull
    public String toGenericString() { return executable.toGenericString(); }

    @NotNull
    public Annotation@NotNull[]@NotNull[] getParameterAnnotations() {
        return executable.getParameterAnnotations();
    }

    @NotNull
    public AnnotatedType getAnnotatedReturnType() {
        return executable.getAnnotatedReturnType();
    }

    public void setAccessible(boolean flag) {
        executable.setAccessible(flag);
    }

    @Override
    public @NotNull Member getMember() { return executable; }
}
