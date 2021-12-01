package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShortVisualConfig extends NumberVisualConfig<Short> {
    private final short min;
    private final short max;

    public ShortVisualConfig(@Nullable Component component, short min, short max) {
        this(component, null, (short) 0, min, max);
    }

    public ShortVisualConfig(@Nullable Component component, @Nullable Short initialValue, @Nullable Short defaultValue, short min, short max) {
        super(component, initialValue, defaultValue);
        this.min = min;
        this.max = max;
    }

    public short getMin() {
        return min;
    }

    public short getMax() {
        return max;
    }

    @NotNull
    @Override
    public Short getMinAsNumber() {
        return min;
    }

    @NotNull
    @Override
    public Short getMaxAsNumber() {
        return max;
    }
}
