package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.Tag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class TagImpl implements Tag {
    private final net.minecraft.nbt.Tag handle;

    public TagImpl(@NotNull net.minecraft.nbt.Tag handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    @NotNull
    public net.minecraft.nbt.Tag getHandle() {
        return handle;
    }

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static Tag of(@NotNull net.minecraft.nbt.Tag tag) {
        if (tag instanceof StringTag stringTag) {
            return new TagStringImpl(stringTag);
        } else if (tag instanceof ByteTag byteTag) {
            return new TagByteImpl(byteTag);
        } else if (tag instanceof IntTag intTag) {
            return new TagIntImpl(intTag);
        } else if (tag instanceof LongTag longTag) {
            return new TagLongImpl(longTag);
        } else if (tag instanceof ShortTag shortTag) {
            return new TagShortImpl(shortTag);
        } else if (tag instanceof FloatTag floatTag) {
            return new TagFloatImpl(floatTag);
        } else if (tag instanceof DoubleTag doubleTag) {
            return new TagDoubleImpl(doubleTag);
        } else if (tag instanceof CompoundTag compoundTag) {
            return new TagCompoundImpl(compoundTag);
        } else if (tag instanceof ListTag listTag) {
            return new TagListImpl(listTag);
        } else if (tag instanceof ByteArrayTag byteArrayTag) {
            return new TagByteArrayImpl(byteArrayTag);
        } else if (tag instanceof IntArrayTag intArrayTag) {
            return new TagIntArrayImpl(intArrayTag);
        } else if (tag instanceof LongArrayTag longArrayTag) {
            return new TagLongArrayImpl(longArrayTag);
        } else if (tag instanceof EndTag) {
            return TagEndImpl.INSTANCE;
        } else {
            throw new IllegalArgumentException("Unknown tag type: " + tag.getClass().getName());
        }
    }

    @Override
    public String toString() {
        return handle.toString();
    }
}
