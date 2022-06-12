package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagString;
import net.blueberrymc.util.Reflected;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.StringTag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class TagStringImpl extends TagImpl implements TagString {
    public TagStringImpl(@NotNull StringTag handle) {
        super(handle);
    }

    @Contract("_ -> new")
    @Reflected
    public static @NotNull TagStringImpl valueOf(@NotNull String value) {
        return new TagStringImpl(StringTag.valueOf(value));
    }

    @NotNull
    @Override
    public StringTag getHandle() {
        return (StringTag) super.getHandle();
    }

    @Override
    public @NotNull String get() {
        return getHandle().getAsString();
    }

    @Override
    public @NotNull TagString set(@NotNull String value) {
        return valueOf(value);
    }

    @Override
    public @NotNull String string() {
        return get();
    }

    @Override
    public <T, DX extends Exception> @NotNull T get(@NotNull Codec<T, String, DX, ?> codec) throws DX {
        return codec.decode(get());
    }
}
