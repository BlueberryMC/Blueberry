package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@ApiStatus.NonExtendable
public interface TagLongArray extends AbstractTagCollection<TagLong> {
    @NotNull
    Codec<TagLongArray, long[], RuntimeException, RuntimeException> CODEC = Codec.codec(TagLongArray::of, TagLongArray::toPrimitiveArray);

    @Contract(pure = true)
    static @NotNull TagLongArray of(@NotNull TagLong @NotNull ... tags) {
        return of(Arrays.stream(tags).map(TagLong::get).toList());
    }

    @SuppressWarnings("RedundantCast")
    @Contract(pure = true)
    static @NotNull TagLongArray of(long @NotNull ... longArray) {
        return (TagLongArray) ImplGetter.byMethod("of", long[].class).apply((Object) longArray);
    }

    @Contract(pure = true)
    static @NotNull TagLongArray of(@NotNull List<@NotNull Long> longList) {
        return (TagLongArray) ImplGetter.byMethod("of", List.class).apply(longList);
    }

    @Override
    default long @NotNull [] toPrimitiveArray() {
        return stream().mapToLong(TagLong::get).toArray();
    }
}
