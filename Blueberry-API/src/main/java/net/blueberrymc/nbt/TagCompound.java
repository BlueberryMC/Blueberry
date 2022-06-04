package net.blueberrymc.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TagCompound extends Tag {
    void put(@NotNull String key, @Nullable Tag tag);
}
