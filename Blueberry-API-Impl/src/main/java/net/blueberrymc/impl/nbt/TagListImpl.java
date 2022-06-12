package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.Tag;
import net.blueberrymc.nbt.TagList;
import net.blueberrymc.util.Reflected;
import net.blueberrymc.util.RetypedIterator;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public class TagListImpl extends AbstractTagCollectionImpl<Tag> implements TagList {
    public TagListImpl(@NotNull ListTag handle) {
        super(handle);
    }

    @Contract(pure = true)
    @Reflected
    public static @NotNull TagList of(@NotNull Tag @NotNull ... tags) {
        var tagList = new TagListImpl(new ListTag());
        tagList.addAll(Arrays.asList(tags));
        return tagList;
    }

    @NotNull
    @Override
    public ListTag getHandle() {
        return (ListTag) super.getHandle();
    }

    @Override
    public @NotNull Tag set(int index, @NotNull Tag value) {
        getHandle().set(index, ((TagImpl) value).getHandle());
        return value;
    }

    @Override
    public void add(int index, @NotNull Tag value) {
        getHandle().add(index, ((TagImpl) value).getHandle());
    }

    @Override
    public @NotNull Tag removeAt(int index) {
        return TagImpl.of(getHandle().remove(index));
    }

    @Override
    public boolean setTag(int index, @NotNull Tag value) {
        return getHandle().setTag(index, ((TagImpl) value).getHandle());
    }

    @Override
    public boolean addTag(int index, @NotNull Tag value) {
        return getHandle().addTag(index, ((TagImpl) value).getHandle());
    }

    @Override
    public int size() {
        return getHandle().size();
    }

    @NotNull
    @Override
    public Iterator<Tag> iterator() {
        return new RetypedIterator<>(getHandle().iterator(), TagImpl::of);
    }

    @Override
    public boolean add(Tag tag) {
        return getHandle().add(((TagImpl) tag).getHandle());
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
