package net.blueberrymc.common.util.reflect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

public class RefField<T> {
    @NotNull
    private final Field field;

    public RefField(@NotNull Field field) {
        this.field = field;
    }

    @Contract(pure = true)
    @NotNull
    public String getName() { return this.field.getName(); }

    @NotNull
    public Field getField() { return this.field; }

    @Contract(pure = true)
    public Object get(@Nullable T t) { try { return this.field.get(t); } catch (IllegalAccessException e) { throw new RuntimeException(e); } }

    /**
     * @deprecated unchecked class, may throw exception at runtime.
     * @param t the instance
     * @return field value
     */
    @Contract(pure = true)
    @Deprecated
    public Object getObj(@Nullable Object t) { try { return this.field.get(t); } catch (IllegalAccessException e) { throw new RuntimeException(e); } }

    public void set(@Nullable T t, Object o) { try { this.field.set(t, o); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setObj(@Nullable Object obj, Object o) { try { this.field.set(obj, o); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setAccessible(boolean flag) { this.field.setAccessible(flag); }

    @NotNull
    @Contract("_ -> this")
    public RefField<T> accessible(boolean flag) { this.field.setAccessible(flag); return this; }

    public void setBoolean(@Nullable T t, boolean flag) { try { this.field.setBoolean(t, flag); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setInt(@Nullable T t, int i) { try { this.field.setInt(t, i); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setByte(@Nullable T t, byte i) { try { this.field.setByte(t, i); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setChar(@Nullable T t, char i) { try { this.field.setChar(t, i); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setFloat(@Nullable T t, float i) { try { this.field.setFloat(t, i); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setLong(@Nullable T t, long i) { try { this.field.setLong(t, i); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setShort(@Nullable T t, short i) { try { this.field.setShort(t, i); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public void setDouble(@Nullable T t, double i) { try { this.field.setDouble(t, i); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public boolean getBoolean(@Nullable T t) { try { return this.field.getBoolean(t); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public int getInt(@Nullable T t) { try { return this.field.getInt(t); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public byte getByte(@Nullable T t) { try { return this.field.getByte(t); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public char getChar(@Nullable T t) { try { return this.field.getChar(t); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public float getFloat(@Nullable T t) { try { return this.field.getFloat(t); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public long getLong(@Nullable T t) { try { return this.field.getLong(t); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public short getShort(@Nullable T t) { try { return this.field.getShort(t); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    public double getDouble(@Nullable T t) { try { return this.field.getDouble(t); } catch (ReflectiveOperationException e) { throw new RuntimeException(e); } }

    @Contract(pure = true)
    public int getModifiers() { return this.field.getModifiers(); }

    @SuppressWarnings("unchecked")
    public Class<T> getDeclaringClass() { return (Class<T>) this.field.getDeclaringClass(); }

    @Contract(pure = true)
    public boolean isEnumConstant() { return this.field.isEnumConstant(); }

    @Contract(pure = true)
    @NotNull
    public String toGenericString() { return this.field.toGenericString(); }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() { return this.field.toString(); }

    @NotNull
    public Annotation[] getAnnotations() { return this.field.getAnnotations(); }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) { return this.field.getAnnotation(annotationClass); }

    @NotNull
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) { return this.field.getAnnotationsByType(annotationClass); }

    @Contract(pure = true)
    @NotNull
    public AnnotatedType getAnnotatedType() { return this.field.getAnnotatedType(); }

    @NotNull
    @Contract(pure = true)
    public Class<?> getType() { return this.field.getType(); }

    @NotNull
    @Contract(pure = true)
    public Type getGenericType() { return this.field.getGenericType(); }
}
