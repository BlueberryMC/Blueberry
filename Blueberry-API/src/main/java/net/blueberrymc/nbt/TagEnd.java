package net.blueberrymc.nbt;

import net.blueberrymc.common.internal.util.ImplGetter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface TagEnd extends Tag {
    @NotNull TagEnd INSTANCE = (TagEnd) ImplGetter.field("INSTANCE");
}
