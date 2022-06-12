package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagList extends AbstractTagCollection<Tag> {
    @Contract(pure = true)
    static @NotNull TagList of(@NotNull Tag @NotNull ... tags) {
        return (TagList) ImplGetter.byMethod("of", Tag[].class).apply(tags);
    }
}
