package net.blueberrymc.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagNumber extends Tag {
    @NotNull
    Number getAsNumber();

    default int getAsInt() {
        return getAsNumber().intValue();
    }

    default float getAsFloat() {
        return getAsNumber().floatValue();
    }

    default double getAsDouble() {
        return getAsNumber().doubleValue();
    }

    default long getAsLong() {
        return getAsNumber().longValue();
    }

    default short getAsShort() {
        return getAsNumber().shortValue();
    }

    default byte getAsByte() {
        return getAsNumber().byteValue();
    }
}
