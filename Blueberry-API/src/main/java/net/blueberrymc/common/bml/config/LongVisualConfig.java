package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LongVisualConfig extends NumberVisualConfig<Long> {
    private final long min;
    private final long max;

    public LongVisualConfig(@Nullable Component component, long min, long max) {
        this(component, null, 0L, min, max);
    }

    public LongVisualConfig(@Nullable Component component, @Nullable Long initialValue, @Nullable Long defaultValue, long min, long max) {
        super(component, initialValue, defaultValue);
        this.min = min;
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    @NotNull
    @Override
    public Long getMinAsNumber() {
        return min;
    }

    @NotNull
    @Override
    public Long getMaxAsNumber() {
        return max;
    }
}
