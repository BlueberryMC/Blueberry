package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagShort extends TagNumber {
    @Contract("_ -> new")
    static @NotNull TagShort valueOf(short value) {
        return (TagShort) ImplGetter.byMethod("valueOf", short.class).apply(value);
    }

    short get();

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    TagShort set(short value);

    @Override
    default @NotNull Number getAsNumber() {
        return get();
    }

    @Override
    default short getAsShort() {
        return get();
    }
}