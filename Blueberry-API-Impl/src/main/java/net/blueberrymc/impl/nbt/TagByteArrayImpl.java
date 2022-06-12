package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.Tag;
import net.blueberrymc.nbt.TagByte;
import net.blueberrymc.nbt.TagByteArray;
import net.blueberrymc.util.Reflected;
import net.blueberrymc.util.RetypedIterator;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.ByteArrayTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class TagByteArrayImpl extends AbstractTagCollectionImpl<TagByte> implements TagByteArray {
    public TagByteArrayImpl(@NotNull ByteArrayTag handle) {
        super(handle);
    }

    @Contract(pure = true)
    @Reflected
    public static @NotNull TagByteArray of(byte @NotNull ... bytes) {
        return new TagByteArrayImpl(new ByteArrayTag(bytes));
    }

    @Contract(pure = true)
    @Reflected
    public static @NotNull TagByteArray of(@NotNull List<@NotNull Byte> bytes) {
        return new TagByteArrayImpl(new ByteArrayTag(bytes));
    }

    @NotNull
    @Override
    public ByteArrayTag getHandle() {
        return (ByteArrayTag) super.getHandle();
    }

    @Override
    public @NotNull TagByte set(int index, @NotNull TagByte value) {
        getHandle().set(index, ((TagByteImpl) value).getHandle());
        return value;
    }

    @Override
    public void add(int index, @NotNull TagByte value) {
        getHandle().add(index, ((TagByteImpl) value).getHandle());
    }

    @Override
    public @NotNull TagByte removeAt(int index) {
        return new TagByteImpl(getHandle().remove(index));
    }

    @Override
    public boolean setTag(int index, @NotNull Tag value) {
        return getHandle().setTag(index, ((TagByteImpl) value).getHandle());
    }

    @Override
    public boolean addTag(int index, @NotNull Tag value) {
        return getHandle().addTag(index, ((TagByteImpl) value).getHandle());
    }

    @Override
    public int size() {
        return getHandle().size();
    }

    @NotNull
    @Override
    public Iterator<TagByte> iterator() {
        return new RetypedIterator<>(getHandle().iterator(), TagByteImpl::new);
    }

    @Override
    public boolean add(TagByte tagByte) {
        return getHandle().add(((TagByteImpl) tagByte).getHandle());
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
