package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagLong extends TagNumber {
    @NotNull
    Codec<TagLong, Long, RuntimeException, RuntimeException> CODEC = Codec.codec(TagLong::valueOf, TagLong::get);

    @Contract("_ -> new")
    static @NotNull TagLong valueOf(long value) {
        return (TagLong) ImplGetter.byMethod("valueOf", long.class).apply(value);
    }

    long get();

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    TagLong set(long value);

    @Override
    default @NotNull Number getAsNumber() {
        return get();
    }

    @Override
    default long getAsLong() {
        return get();
    }
}
