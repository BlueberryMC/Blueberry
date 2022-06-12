package net.blueberrymc.impl.nbt;

import net.blueberrymc.nbt.TagEnd;
import net.kyori.adventure.util.Codec;
import net.minecraft.nbt.EndTag;
import org.jetbrains.annotations.NotNull;

public class TagEndImpl extends TagImpl implements TagEnd {
    public static final TagEndImpl INSTANCE = new TagEndImpl(EndTag.INSTANCE);

    public TagEndImpl(@NotNull EndTag handle) {
        super(handle);
    }

    @NotNull
    @Override
    public EndTag getHandle() {
        return (EndTag) super.getHandle();
    }

    @Override
    public @NotNull String string() {
        return "END";
    }

    @Override
    public <T, DX extends Exception> @NotNull T get(@NotNull Codec<T, String, DX, ?> codec) throws DX {
        return codec.decode("END");
    }
}
