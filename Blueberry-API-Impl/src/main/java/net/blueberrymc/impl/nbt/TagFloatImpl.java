package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagFloat;
import net.blueberrymc.util.Reflected;
import net.minecraft.nbt.FloatTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TagFloatImpl extends TagNumberImpl implements TagFloat {
    public TagFloatImpl(@NotNull FloatTag handle) {
        super(handle);
    }

    @Contract("_ -> new")
    @Reflected
    public static @NotNull TagFloatImpl valueOf(float value) {
        return new TagFloatImpl(FloatTag.valueOf(value));
    }

    @NotNull
    @Override
    public FloatTag getHandle() {
        return (FloatTag) super.getHandle();
    }

    @Override
    public float get() {
        return getHandle().getAsFloat();
    }

    @Override
    public @NotNull TagFloat set(float value) {
        return valueOf(value);
    }
}
