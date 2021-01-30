package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.TypeVariable;

public class RefExecutable implements RefMember {
    private final Executable executable;

    public RefExecutable(Executable executable) {
        this.executable = executable;
    }

    @NotNull
    public Executable getExecutable() { return executable; }

    @NotNull
    public Class<?> getDeclaringClass() { return executable.getDeclaringClass(); }

    public String getName() { return executable.getName(); }

    public int getModifiers() { return executable.getModifiers(); }

    public TypeVariable<?>[] getTypeParameters() { return executable.getTypeParameters(); }

    public Class<?>[] getParameterTypes() { return executable.getParameterTypes(); }

    public Class<?>[] getExceptionTypes() { return executable.getExceptionTypes(); }

    public String toGenericString() { return executable.toGenericString(); }

    public Annotation[][] getParameterAnnotations() {
        return executable.getParameterAnnotations();
    }

    public AnnotatedType getAnnotatedReturnType() {
        return executable.getAnnotatedReturnType();
    }

    public void setAccessible(boolean flag) {
        executable.setAccessible(flag);
    }

    @Override
    public @NotNull Member getMember() { return executable; }
}
