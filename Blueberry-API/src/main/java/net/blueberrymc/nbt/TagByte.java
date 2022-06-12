package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagByte extends TagNumber {
    @Contract("_ -> new")
    static @NotNull TagByte valueOf(byte value) {
        return (TagByte) ImplGetter.byMethod("valueOf", byte.class).apply(value);
    }

    byte get();

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    TagByte set(byte value);

    @Override
    default @NotNull Number getAsNumber() {
        return get();
    }

    @Override
    default byte getAsByte() {
        return get();
    }
}
