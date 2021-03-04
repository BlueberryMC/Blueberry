package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class VisualConfig<T> {
    private final Component component;
    private T value;
    private final T defaultValue;

    public VisualConfig(@Nullable Component component) {
        this(component, null);
    }

    public VisualConfig(@Nullable Component component, @Nullable T initialValue) {
        this.component = component;
        this.value = this.defaultValue = initialValue;
    }

    @Nullable
    public Component getComponent() {
        return component;
    }

    public void set(@Nullable T value) {
        this.value = value;
    }

    @Nullable
    public T get() {
        return this.value;
    }

    @Nullable
    public T getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    private Component description;

    @NotNull
    public VisualConfig<T> description(@Nullable Component description) {
        this.description = description;
        return this;
    }

    @Nullable
    public Component getDescription() {
        return description;
    }

    // you can use it for anything like storing config path, etc.
    private String id;

    @NotNull
    public VisualConfig<T> id(@Nullable String id) {
        this.id = id;
        return this;
    }

    @Nullable
    public String getId() {
        return id;
    }
}
