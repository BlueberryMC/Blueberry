package net.blueberrymc.common.bml.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
