package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@ApiStatus.NonExtendable
public interface TagIntArray extends AbstractTagCollection<TagInt> {
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
}
