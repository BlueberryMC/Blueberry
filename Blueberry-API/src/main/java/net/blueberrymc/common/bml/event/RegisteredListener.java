package net.blueberrymc.common.bml.event;

import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.util.ThrowableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RegisteredListener(
        @NotNull ThrowableConsumer<Event> executor,
        @NotNull EventPriority priority,
        @Nullable Object listener,
        @NotNull BlueberryMod mod) {
}
