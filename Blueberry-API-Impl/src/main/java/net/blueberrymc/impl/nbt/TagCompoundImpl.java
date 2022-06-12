package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.Tag;
import net.blueberrymc.nbt.TagCompound;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TagCompoundImpl extends TagImpl implements TagCompound {
    public TagCompoundImpl(@NotNull CompoundTag handle) {
        super(handle);
    }

    @Contract(value = "null -> null; !null -> new", pure = true)
    public static TagCompoundImpl of(@Nullable CompoundTag handle) {
        if (handle == null) {
            return null;
        }
        return new TagCompoundImpl(handle);
    }

    @NotNull
    @Override
    public CompoundTag getHandle() {
        return (CompoundTag) super.getHandle();
    }

    @Override
    public void remove(@NotNull String key) {
        getHandle().remove(key);
    }

    @Override
    public void put(@NotNull String key, @NotNull Tag tag) {
        getHandle().put(key, ((TagImpl) tag).getHandle());
    }

    @Override
    public @Nullable Tag get(@NotNull String key) {
        var tag = getHandle().get(key);
        if (tag == null) {
            return null;
        }
        return TagImpl.of(tag);
    }

    @Override
    public @NotNull String string() {
        return getHandle().getAsString();
    }

    @Override
    public <T, DX extends Exception> @NotNull T get(@NotNull Codec<T, String, DX, ?> codec) throws DX {
        return codec.decode(string());
    }
}
