package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@ApiStatus.NonExtendable
public interface TagByteArray extends AbstractTagCollection<TagByte> {
    @Contract(pure = true)
    static @NotNull TagByteArray of(@NotNull TagByte @NotNull ... tags) {
        return of(Arrays.stream(tags).map(TagByte::get).toList());
    }

    @SuppressWarnings("RedundantCast")
    @Contract(pure = true)
    static @NotNull TagByteArray of(byte @NotNull ... bytes) {
        return (TagByteArray) ImplGetter.byMethod("of", byte[].class).apply((Object) bytes);
    }

    @Contract(pure = true)
    static @NotNull TagByteArray of(@NotNull List<@NotNull Byte> bytes) {
        return (TagByteArray) ImplGetter.byMethod("of", List.class).apply(bytes);
    }
}
