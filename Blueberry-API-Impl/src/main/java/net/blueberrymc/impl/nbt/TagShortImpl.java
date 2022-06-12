package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagShort;
import net.blueberrymc.util.Reflected;
import net.minecraft.nbt.ShortTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TagShortImpl extends TagNumberImpl implements TagShort {
    public TagShortImpl(@NotNull ShortTag handle) {
        super(handle);
    }

    @Contract("_ -> new")
    @Reflected
    public static @NotNull TagShortImpl valueOf(short value) {
        return new TagShortImpl(ShortTag.valueOf(value));
    }

    @NotNull
    @Override
    public ShortTag getHandle() {
        return (ShortTag) super.getHandle();
    }

    @Override
    public short get() {
        return getHandle().getAsShort();
    }

    @Override
    public @NotNull TagShort set(short value) {
        return valueOf(value);
    }
}
