package net.blueberrymc.common.bml.event;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.DeprecatedReason;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.util.ThrowableConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HandlerList {
    @NotNull
    private final Set<RegisteredListener> listeners = Collections.synchronizedSet(new HashSet<>());

    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Listener interface is deprecated")
    @Deprecated(forRemoval = true)
    public void add(@NotNull ThrowableConsumer<@NotNull Event> consumer, @NotNull EventPriority priority, @Nullable Listener listener, @NotNull BlueberryMod mod) {
        this.add(consumer, priority, (Object) listener, mod);
    }

    public void add(@NotNull ThrowableConsumer<@NotNull Event> consumer, @NotNull EventPriority priority, @Nullable Object listener, @NotNull BlueberryMod mod) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(priority, "priority cannot be null");
        Preconditions.checkNotNull(mod, "mod cannot be null");
        listeners.add(new RegisteredListener(consumer, priority, listener, mod));
    }

    public void remove(@NotNull BlueberryMod mod) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        List<RegisteredListener> toRemove = new ArrayList<>();
        listeners.forEach(registeredListener -> {
            if (mod.equals(registeredListener.mod())) {
                toRemove.add(registeredListener);
            }
        });
        toRemove.forEach(listeners::remove);
    }

    public boolean anyContains(@NotNull BlueberryMod mod) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        for (RegisteredListener listener : listeners) {
            if (mod.equals(listener.mod())) return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public int size() {
        return listeners.size();
    }

    /**
     * @deprecated Listener interface is deprecated. Use {@link #removeListener(Object)} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @DeprecatedReason("Listener interface is deprecated")
    @Deprecated(forRemoval = true)
    public void remove(@NotNull Listener listener) {
        removeListener(listener);
    }

    public void removeListener(@NotNull Object listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        List<RegisteredListener> toRemove = new ArrayList<>();
        listeners.forEach(registeredListener -> {
            if (listener.equals(registeredListener.listener())) {
                toRemove.add(registeredListener);
            }
        });
        toRemove.forEach(listeners::remove);
    }

    public void fire(@NotNull Event event) {
        Preconditions.checkNotNull(event, "event cannot be null");
        this.listeners
                .stream()
                .sorted(Comparator.comparingInt(registeredListener -> registeredListener.priority().getSlot()))
                .forEach(registeredListener -> {
                    try {
                        registeredListener.executor().accept(event);
                    } catch (Throwable e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        String listenerName = registeredListener.listener() == null ? null : registeredListener.listener().getClass().getTypeName();
                        EventManager.printStackTrace(new EventException("Could not pass event " + event.getEventTypeName() + " to listener " + listenerName + " of mod " + registeredListener.mod().getName(), cause));
                    }
                });
    }
}
