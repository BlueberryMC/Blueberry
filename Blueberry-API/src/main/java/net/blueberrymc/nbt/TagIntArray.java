package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@ApiStatus.NonExtendable
public interface TagIntArray extends AbstractTagCollection<TagInt> {
    @NotNull
    Codec<TagIntArray, int[], RuntimeException, RuntimeException> CODEC = Codec.codec(TagIntArray::of, TagIntArray::toPrimitiveArray);

    @Contract(pure = true)
    static @NotNull TagIntArray of(@NotNull TagInt @NotNull ... tags) {
        return of(Arrays.stream(tags).map(TagInt::get).toList());
    }

    @SuppressWarnings("RedundantCast")
    @Contract(pure = true)
    static @NotNull TagIntArray of(int @NotNull ... intArray) {
        return (TagIntArray) ImplGetter.byMethod("of", int[].class).apply((Object) intArray);
    }

    @Contract(pure = true)
    static @NotNull TagIntArray of(@NotNull List<@NotNull Integer> intList) {
        return (TagIntArray) ImplGetter.byMethod("of", List.class).apply(intList);
    }

    @Override
    default int @NotNull [] toPrimitiveArray() {
        return stream().mapToInt(TagInt::get).toArray();
    }
}
