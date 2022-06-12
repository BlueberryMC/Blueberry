package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TagParser {
    /**
     * Parses a compound tag from a string.
     * @param nbt SNBT string
     * @return Compound tag
     * @throws TagParserException If the string is not a valid SNBT string
     */
    @Contract(pure = true)
    public static @NotNull TagCompound parse(@NotNull String nbt) throws TagParserException {
        return (TagCompound) ImplGetter.byMethod("parse", String.class).apply(nbt);
    }

    public static final class TagParserException extends Exception {
        public TagParserException(@NotNull Throwable cause) {
            super(cause);
        }
    }
}
