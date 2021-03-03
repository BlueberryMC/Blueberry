package net.blueberrymc.common.bml.event;

import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.util.ThrowableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegisteredListener {
    @NotNull private final ThrowableConsumer<@NotNull Event> executor;
    @NotNull private final EventPriority priority;
    @Nullable private final Listener listener;
    @NotNull private final BlueberryMod mod;

    public RegisteredListener(
            @NotNull ThrowableConsumer<@NotNull Event> executor,
            @NotNull EventPriority priority,
            @Nullable Listener listener,
            @NotNull BlueberryMod mod) {
        this.executor = executor;
        this.priority = priority;
        this.listener = listener;
        this.mod = mod;
    }

    @NotNull
    public ThrowableConsumer<Event> getExecutor() {
        return executor;
    }

    @NotNull
    public EventPriority getPriority() {
        return priority;
    }

    @Nullable
    public Listener getListener() {
        return listener;
    }

    @NotNull
    public BlueberryMod getMod() {
        return mod;
    }
}
