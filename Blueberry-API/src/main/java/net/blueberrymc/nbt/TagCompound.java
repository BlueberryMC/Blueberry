package net.blueberrymc.nbt;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface TagCompound extends Tag {
    /**
     * Removes the tag with the given name.
     * @param key The name of the tag to remove.
     */
    @Contract(mutates = "this")
    void remove(@NotNull String key);

    /**
     * Puts a tag into this compound.
     * @param key The name of the tag.
     * @param tag The tag to put.
     */
    @Contract(mutates = "this")
    void put(@NotNull String key, @NotNull Tag tag);

    /**
     * Puts a string into this compound.
     * @param key The name of the tag.
     * @param value The value of the tag.
     */
    default void putString(@NotNull String key, @NotNull String value) {
        put(key, TagString.valueOf(value));
    }

    /**
     * Puts a byte into this compound.
     * @param key The name of the tag.
     * @param value The value of the tag.
     */
    default void putByte(@NotNull String key, byte value) {
        put(key, TagByte.valueOf(value));
    }

    /**
     * Puts a short into this compound.
     * @param key The name of the tag.
     * @param value The value of the tag.
     */
    default void putShort(@NotNull String key, short value) {
        put(key, TagShort.valueOf(value));
    }

    /**
     * Puts an integer into this compound.
     * @param key The name of the tag.
     * @param value The value of the tag.
     */
    default void putInt(@NotNull String key, int value) {
        put(key, TagInt.valueOf(value));
    }

    /**
     * Puts a long into this compound.
     * @param key The name of the tag.
     * @param value The value of the tag.
     */
    default void putLong(@NotNull String key, long value) {
        put(key, TagLong.valueOf(value));
    }

    /**
     * Puts a float into this compound.
     * @param key The name of the tag.
     * @param value The value of the tag.
     */
    default void putFloat(@NotNull String key, float value) {
        put(key, TagFloat.valueOf(value));
    }

    /**
     * Puts a double into this compound.
     * @param key The name of the tag.
     * @param value The value of the tag.
     */
    default void putDouble(@NotNull String key, double value) {
        put(key, TagDouble.valueOf(value));
    }

    /**
     * Puts a boolean into this compound.
     * @param key The name of the tag.
     * @param value The value of the tag.
     */
    default void putBoolean(@NotNull String key, boolean value) {
        put(key, TagByte.valueOf((byte) (value ? 1 : 0)));
    }

    /**
     * Gets a tag from this compound.
     * @param key The name of the tag.
     * @return The tag with the given name, or null if it doesn't exist.
     */
    @Nullable
    Tag get(@NotNull String key);
}
