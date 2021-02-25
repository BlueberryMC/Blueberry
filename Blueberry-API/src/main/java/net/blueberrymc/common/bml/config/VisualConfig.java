package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class VisualConfig<T> {
    private final Component component;
    private T value;
    private final T defaultValue;

    public VisualConfig(Component component) {
        this(component, null);
    }

    public VisualConfig(Component component, T initialValue) {
        this.component = component;
        this.value = this.defaultValue = initialValue;
    }

    public Component getComponent() {
        return component;
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    private Component description;

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

    public VisualConfig<T> id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }
}
