package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FloatVisualConfig extends NumberVisualConfig<Float> {
    private final float min;
    private final float max;

    public FloatVisualConfig(@Nullable Component component, float min, float max) {
        this(component, null, 0F, min, max);
    }

    public FloatVisualConfig(@Nullable Component component, @Nullable Float initialValue, @Nullable Float defaultValue, float min, float max) {
        super(component, initialValue, defaultValue);
        this.min = min;
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    @NotNull
    @Override
    public Float getMinAsNumber() {
        return min;
    }

    @NotNull
    @Override
    public Float getMaxAsNumber() {
        return max;
    }
}
