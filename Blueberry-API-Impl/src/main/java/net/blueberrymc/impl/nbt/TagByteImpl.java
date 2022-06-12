package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagByte;
import net.blueberrymc.util.Reflected;
import net.minecraft.nbt.ByteTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TagByteImpl extends TagNumberImpl implements TagByte {
    public TagByteImpl(@NotNull ByteTag handle) {
        super(handle);
    }

    @Contract("_ -> new")
    @Reflected
    public static @NotNull TagByteImpl valueOf(byte value) {
        return new TagByteImpl(ByteTag.valueOf(value));
    }

    @NotNull
    @Override
    public ByteTag getHandle() {
        return (ByteTag) super.getHandle();
    }

    @Override
    public byte get() {
        return getHandle().getAsByte();
    }

    @Override
    public @NotNull TagByte set(byte value) {
        return valueOf(value);
    }
}
