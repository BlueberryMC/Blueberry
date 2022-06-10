package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public abstract class NumberVisualConfig<T extends Number> extends VisualConfig<T> {
    protected NumberVisualConfig(@Nullable Component component, @Nullable T initialValue, @Nullable T defaultValue) {
        super(component, initialValue, defaultValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(@Nullable Number value) {
        super.set((T) value);
    }

    public abstract void setPercentage(@Range(from = 0, to = 1) double percentage);

    @Range(from = 0, to = 1)
    public double getPercentage() {
        if (get() == null) {
            return 0;
        }
        double p = getPercentage0();
        if (p < 0) {
            return 0;
        }
        if (p > 1) {
            return 1;
        }
        return p;
    }

    @Range(from = 0, to = 1)
    protected abstract double getPercentage0();

    @NotNull
    public abstract Number getMaxAsNumber();

    @NotNull
    public abstract Number getMinAsNumber();
}
