package net.blueberrymc.common.bml.config;

import net.blueberrymc.common.util.DeprecatedData;
import net.blueberrymc.common.util.ExperimentalData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class VisualConfig<T> {
    private final Component component;
    private T value;
    private final T defaultValue;
    private boolean requiresRestart;
    private DeprecatedData deprecatedData = DeprecatedData.NOT_DEPRECATED;
    private ExperimentalData experimentalData = ExperimentalData.NOT_EXPERIMENTAL;

    protected VisualConfig(@Nullable Component component) {
        this(component, null, null);
    }

    protected VisualConfig(@Nullable Component component, @Nullable T initialValue, @Nullable T defaultValue) {
        this.component = component;
        this.value = initialValue == null ? defaultValue : initialValue;
        this.defaultValue = defaultValue;
    }

    @Nullable
    public Component getComponent() {
        return component;
    }

    @Contract(mutates = "this")
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

    @Contract(mutates = "this")
    @NotNull
    public VisualConfig<T> description(@Nullable String description) {
        this.description = description != null ? Component.text(description) : null;
        return this;
    }

    @Contract(mutates = "this")
    @NotNull
    public VisualConfig<T> description(@Nullable Component description) {
        this.description = description;
        return this;
    }

    @Nullable
    public Component getDescription() {
        return description;
    }

    @Contract(mutates = "this")
    @NotNull
    public VisualConfig<T> requiresRestart(boolean flag) {
        this.requiresRestart = flag;
        return this;
    }

    @Contract(mutates = "this")
    @NotNull
    public VisualConfig<T> requiresRestart() {
        return this.requiresRestart(true);
    }

    public boolean isRequiresRestart() {
        return this.requiresRestart;
    }

    @Contract(mutates = "this")
    @NotNull
    public VisualConfig<T> deprecated(@Nullable DeprecatedData deprecatedData) {
        this.deprecatedData = Objects.requireNonNullElse(deprecatedData, DeprecatedData.NOT_DEPRECATED);
        return this;
    }

    @Contract(mutates = "this")
    @NotNull
    public VisualConfig<T> experimental(@Nullable ExperimentalData experimentalData) {
        this.experimentalData = Objects.requireNonNullElse(experimentalData, ExperimentalData.NOT_EXPERIMENTAL);
        return this;
    }

    @NotNull
    public DeprecatedData getDeprecatedData() {
        return this.deprecatedData;
    }

    @NotNull
    public ExperimentalData getExperimentalData() {
        return this.experimentalData;
    }

    // you can use it for anything like storing config path, etc.
    private String id;

    @Contract(mutates = "this")
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
