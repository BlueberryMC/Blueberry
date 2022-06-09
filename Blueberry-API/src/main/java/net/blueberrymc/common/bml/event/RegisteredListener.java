package net.blueberrymc.common.bml.event;

import net.blueberrymc.common.DeprecatedReason;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.util.ThrowableConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RegisteredListener(
        @NotNull ThrowableConsumer<Event> executor,
        @NotNull EventPriority priority,
        @Nullable Listener listener,
        @NotNull BlueberryMod mod) {
    /**
     * @deprecated Use {@link #executor()} instead.
     */
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #executor() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @NotNull
    public ThrowableConsumer<Event> getExecutor() {
        return executor;
    }

    /**
     * @deprecated Use {@link #priority()} instead.
     */
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #priority() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @NotNull
    public EventPriority getPriority() {
        return priority;
    }

    /**
     * @deprecated Use {@link #listener()} instead.
     */
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #listener() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Nullable
    public Listener getListener() {
        return listener;
    }

    /**
     * @deprecated Use {@link #mod()} instead.
     */
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #mod() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @NotNull
    public BlueberryMod getMod() {
        return mod;
    }
}
