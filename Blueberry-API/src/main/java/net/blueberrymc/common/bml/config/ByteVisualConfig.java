package net.blueberrymc.common.bml.config;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class ByteVisualConfig extends NumberVisualConfig<Byte> {
    private final byte min;
    private final byte max;

    public ByteVisualConfig(@Nullable Component component, byte min, byte max) {
        this(component, null, (byte) 0, min, max);
    }

    public ByteVisualConfig(@Nullable Component component, @Nullable Byte initialValue, @Nullable Byte defaultValue, byte min, byte max) {
        super(component, initialValue, defaultValue);
        this.min = min;
        this.max = max;
    }

    public byte getMin() {
        return min;
    }

    public byte getMax() {
        return max;
    }

    @Override
    public void setPercentage(@Range(from = 0, to = 1) double percentage) {
        set((byte) (min + ((double) max - min) * percentage));
    }

    @Override
    public @Range(from = 0, to = 1) double getPercentage0() {
        return ((double) Objects.requireNonNull(get()) - min) / ((double) max - min);
    }

    @NotNull
    @Override
    public Byte getMinAsNumber() {
        return min;
    }

    @NotNull
    @Override
    public Byte getMaxAsNumber() {
        return max;
    }
}
