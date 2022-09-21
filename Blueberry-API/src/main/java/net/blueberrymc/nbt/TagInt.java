package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagInt extends TagNumber {
    @NotNull
    Codec<TagInt, Integer, RuntimeException, RuntimeException> CODEC = Codec.codec(TagInt::valueOf, TagInt::get);

    @Contract("_ -> new")
    static @NotNull TagInt valueOf(int value) {
        return (TagInt) ImplGetter.byMethod("valueOf", int.class).apply(value);
    }

    int get();

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    TagInt set(int value);

    @Override
    default @NotNull Number getAsNumber() {
        return get();
    }

    @Override
    default int getAsInt() {
        return get();
    }
}
