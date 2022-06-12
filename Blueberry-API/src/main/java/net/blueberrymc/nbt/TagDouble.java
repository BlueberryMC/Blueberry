package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagDouble extends TagNumber {
    @Contract("_ -> new")
    static @NotNull TagDouble valueOf(double value) {
        return (TagDouble) ImplGetter.byMethod("valueOf", double.class).apply(value);
    }

    double get();

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    TagDouble set(double value);

    @Override
    default @NotNull Number getAsNumber() {
        return get();
    }

    @Override
    default double getAsDouble() {
        return get();
    }
}
