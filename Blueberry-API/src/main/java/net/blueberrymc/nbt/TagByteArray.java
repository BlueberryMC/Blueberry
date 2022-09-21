package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@ApiStatus.NonExtendable
public interface TagByteArray extends AbstractTagCollection<TagByte> {
    @NotNull
    Codec<TagByteArray, byte[], RuntimeException, RuntimeException> CODEC = Codec.codec(TagByteArray::of, TagByteArray::toPrimitiveArray);
    
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

    @Override
    default byte @NotNull [] toPrimitiveArray() {
        byte[] bytes = new byte[size()];
        int i = 0;
        for (TagByte tag : this) {
            bytes[i++] = tag.get();
        }
        return bytes;
    }
}
