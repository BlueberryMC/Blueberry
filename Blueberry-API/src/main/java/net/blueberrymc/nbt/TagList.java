package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ApiStatus.NonExtendable
public interface TagList extends AbstractTagCollection<Tag> {
    @NotNull
    Codec<TagList, Tag[], RuntimeException, RuntimeException> CODEC_ARRAY = Codec.codec(TagList::of, TagList::toPrimitiveArray);

    @NotNull
    Codec<TagList, List<Tag>, RuntimeException, RuntimeException> CODEC_COLLECTION = Codec.codec(l -> of(l.toArray(Tag[]::new)), l -> l.stream().toList());

    @Contract(pure = true)
    static @NotNull TagList of(@NotNull Tag @NotNull ... tags) {
        return (TagList) ImplGetter.byMethod("of", Tag[].class).apply(tags);
    }

    @Override
    default @NotNull Tag @NotNull [] toPrimitiveArray() {
        return toArray(Tag[]::new);
    }
}
