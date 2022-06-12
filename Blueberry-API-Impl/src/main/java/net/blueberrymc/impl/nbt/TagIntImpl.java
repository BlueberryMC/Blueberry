package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagInt;
import net.blueberrymc.util.Reflected;
import net.minecraft.nbt.IntTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TagIntImpl extends TagNumberImpl implements TagInt {
    public TagIntImpl(@NotNull IntTag handle) {
        super(handle);
    }

    @Contract("_ -> new")
    @Reflected
    public static @NotNull TagIntImpl valueOf(int value) {
        return new TagIntImpl(IntTag.valueOf(value));
    }

    @NotNull
    @Override
    public IntTag getHandle() {
        return (IntTag) super.getHandle();
    }

    @Override
    public int get() {
        return getHandle().getAsInt();
    }

    @Override
    public @NotNull TagInt set(int value) {
        return valueOf(value);
    }
}
