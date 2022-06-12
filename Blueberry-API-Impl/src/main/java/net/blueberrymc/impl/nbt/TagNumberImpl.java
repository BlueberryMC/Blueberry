package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagNumber;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.NumericTag;
import org.jetbrains.annotations.NotNull;

public abstract class TagNumberImpl extends TagImpl implements TagNumber {
    public TagNumberImpl(@NotNull NumericTag handle) {
        super(handle);
    }

    @NotNull
    @Override
    public NumericTag getHandle() {
        return (NumericTag) super.getHandle();
    }

    @Override
    public @NotNull String string() {
        return getAsNumber().toString();
    }

    @Override
    public <T, DX extends Exception> @NotNull T get(@NotNull Codec<T, String, DX, ?> codec) throws DX {
        return codec.decode(string());
    }
}
