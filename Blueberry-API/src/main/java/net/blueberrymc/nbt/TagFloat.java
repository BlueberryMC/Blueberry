package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagFloat extends TagNumber {
    @Contract("_ -> new")
    static @NotNull TagFloat valueOf(float value) {
        return (TagFloat) ImplGetter.byMethod("valueOf", float.class).apply(value);
    }

    float get();

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    TagFloat set(float value);

    @Override
    default @NotNull Number getAsNumber() {
        return get();
    }

    @Override
    default float getAsFloat() {
        return get();
    }
}
