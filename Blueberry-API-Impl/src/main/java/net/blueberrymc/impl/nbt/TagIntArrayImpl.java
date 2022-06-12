package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.Tag;
import net.blueberrymc.nbt.TagInt;
import net.blueberrymc.nbt.TagIntArray;
import net.blueberrymc.util.Reflected;
import net.blueberrymc.util.RetypedIterator;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.IntArrayTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class TagIntArrayImpl extends AbstractTagCollectionImpl<TagInt> implements TagIntArray {
    public TagIntArrayImpl(@NotNull IntArrayTag handle) {
        super(handle);
    }

    @Contract(pure = true)
    @Reflected
    public static @NotNull TagIntArray of(int @NotNull ... intArray) {
        return new TagIntArrayImpl(new IntArrayTag(intArray));
    }

    @Contract(pure = true)
    @Reflected
    public static @NotNull TagIntArray of(@NotNull List<@NotNull Integer> intList) {
        return new TagIntArrayImpl(new IntArrayTag(intList));
    }

    @NotNull
    @Override
    public IntArrayTag getHandle() {
        return (IntArrayTag) super.getHandle();
    }

    @Override
    public @NotNull TagInt set(int index, @NotNull TagInt value) {
        getHandle().set(index, ((TagIntImpl) value).getHandle());
        return value;
    }

    @Override
    public void add(int index, @NotNull TagInt value) {
        getHandle().add(index, ((TagIntImpl) value).getHandle());
    }

    @Override
    public @NotNull TagInt removeAt(int index) {
        return new TagIntImpl(getHandle().remove(index));
    }

    @Override
    public boolean setTag(int index, @NotNull Tag value) {
        return getHandle().setTag(index, ((TagIntImpl) value).getHandle());
    }

    @Override
    public boolean addTag(int index, @NotNull Tag value) {
        return getHandle().addTag(index, ((TagIntImpl) value).getHandle());
    }

    @Override
    public int size() {
        return getHandle().size();
    }

    @NotNull
    @Override
    public Iterator<TagInt> iterator() {
        return new RetypedIterator<>(getHandle().iterator(), TagIntImpl::new);
    }

    @Override
    public boolean add(TagInt tagInt) {
        return getHandle().add(((TagIntImpl) tagInt).getHandle());
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
