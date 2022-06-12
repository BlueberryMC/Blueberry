package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagLong;
import net.blueberrymc.util.Reflected;
import net.minecraft.nbt.LongTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TagLongImpl extends TagNumberImpl implements TagLong {
    public TagLongImpl(@NotNull LongTag handle) {
        super(handle);
    }

    @Contract("_ -> new")
    @Reflected
    public static @NotNull TagLongImpl valueOf(long value) {
        return new TagLongImpl(LongTag.valueOf(value));
    }

    @NotNull
    @Override
    public LongTag getHandle() {
        return (LongTag) super.getHandle();
    }

    @Override
    public long get() {
        return getHandle().getAsLong();
    }

    @Override
    public @NotNull TagLong set(long value) {
        return valueOf(value);
    }
}
