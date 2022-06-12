package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagDouble;
import net.blueberrymc.util.Reflected;
import net.minecraft.nbt.DoubleTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TagDoubleImpl extends TagNumberImpl implements TagDouble {
    public TagDoubleImpl(@NotNull DoubleTag handle) {
        super(handle);
    }

    @Contract("_ -> new")
    @Reflected
    public static @NotNull TagDoubleImpl valueOf(double value) {
        return new TagDoubleImpl(DoubleTag.valueOf(value));
    }

    @NotNull
    @Override
    public DoubleTag getHandle() {
        return (DoubleTag) super.getHandle();
    }

    @Override
    public double get() {
        return getHandle().getAsDouble();
    }

    @Override
    public @NotNull TagDouble set(double value) {
        return valueOf(value);
    }
}
