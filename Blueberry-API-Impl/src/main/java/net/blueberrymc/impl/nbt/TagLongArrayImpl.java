package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.Tag;
import net.blueberrymc.nbt.TagLong;
import net.blueberrymc.nbt.TagLongArray;
import net.blueberrymc.util.Reflected;
import net.blueberrymc.util.RetypedIterator;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.LongArrayTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class TagLongArrayImpl extends AbstractTagCollectionImpl<TagLong> implements TagLongArray {
    public TagLongArrayImpl(@NotNull LongArrayTag handle) {
        super(handle);
    }

    @Contract(pure = true)
    @Reflected
    public static @NotNull TagLongArray of(long @NotNull ... longArray) {
        return new TagLongArrayImpl(new LongArrayTag(longArray));
    }

    @Contract(pure = true)
    @Reflected
    public static @NotNull TagLongArray of(@NotNull List<@NotNull Long> longList) {
        return new TagLongArrayImpl(new LongArrayTag(longList));
    }

    @NotNull
    @Override
    public LongArrayTag getHandle() {
        return (LongArrayTag) super.getHandle();
    }

    @Override
    public @NotNull TagLong set(int index, @NotNull TagLong value) {
        getHandle().set(index, ((TagLongImpl) value).getHandle());
        return value;
    }

    @Override
    public void add(int index, @NotNull TagLong value) {
        getHandle().add(index, ((TagLongImpl) value).getHandle());
    }

    @Override
    public @NotNull TagLong removeAt(int index) {
        return new TagLongImpl(getHandle().remove(index));
    }

    @Override
    public boolean setTag(int index, @NotNull Tag value) {
        return getHandle().setTag(index, ((TagLongImpl) value).getHandle());
    }

    @Override
    public boolean addTag(int index, @NotNull Tag value) {
        return getHandle().addTag(index, ((TagLongImpl) value).getHandle());
    }

    @Override
    public int size() {
        return getHandle().size();
    }

    @NotNull
    @Override
    public Iterator<TagLong> iterator() {
        return new RetypedIterator<>(getHandle().iterator(), TagLongImpl::new);
    }

    @Override
    public boolean add(TagLong tagLong) {
        return getHandle().add(((TagLongImpl) tagLong).getHandle());
    }

    @Override
    public @NotNull String string() {
        return getHandle().toString();
    }

    @Override
    public <T, DX extends Exception> @NotNull T get(@NotNull Codec<T, String, DX, ?> codec) throws DX {
        return codec.decode(string());
    }
}
