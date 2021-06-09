package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class ClassVisualConfig extends VisualConfig<Class<?>> {
    @Nullable
    private Pattern pattern = null;

    public ClassVisualConfig(@Nullable Component component) {
        this(component, null, (String) null);
    }

    public ClassVisualConfig(@Nullable Component component, @Nullable String initialValue, @Nullable String defaultValue) {
        super(component, forName(initialValue), forName(defaultValue));
    }

    public ClassVisualConfig(@Nullable Component component, @Nullable Class<?> initialValue, @Nullable Class<?> defaultValue) {
        super(component, initialValue, defaultValue);
    }

    @Contract("null -> null")
    private static Class<?> forName(@Nullable String clazz) {
        if (clazz == null) return null;
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> Class<T> getTyped() {
        return (Class<T>) super.get();
    }

    @Nullable
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public ClassVisualConfig pattern(@Nullable Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    @NotNull
    public ClassVisualConfig pattern(@Language("RegExp") @Nullable String pattern) {
        return pattern(pattern != null ? Pattern.compile(pattern) : null);
    }

    @Contract("null -> false")
    public boolean isValid(@Nullable String s) {
        if (s == null) return false;
        try {
            Class.forName(s);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return this.pattern == null || this.pattern.matcher(s).matches();
    }

    public void set(@NotNull String s) {
        if (!isValid(s)) throw new IllegalArgumentException("Invalid class name: " + s);
        try {
            this.set(Class.forName(s));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
