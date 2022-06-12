package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagString extends Tag {
    @Contract("_ -> new")
    static @NotNull TagString valueOf(@NotNull String value) {
        return (TagString) ImplGetter.byMethod("valueOf", String.class).apply(value);
    }

    @NotNull
    String get();

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    TagString set(@NotNull String value);
}
