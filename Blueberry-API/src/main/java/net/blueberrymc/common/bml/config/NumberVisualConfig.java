package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NumberVisualConfig<T extends Number> extends VisualConfig<T> {
    protected NumberVisualConfig(@Nullable Component component, @Nullable T initialValue, @Nullable T defaultValue) {
        super(component, initialValue, defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(@Nullable Number value) {
        super.set((T) value);
    }

    @NotNull
    public abstract Number getMaxAsNumber();

    @NotNull
    public abstract Number getMinAsNumber();
}
