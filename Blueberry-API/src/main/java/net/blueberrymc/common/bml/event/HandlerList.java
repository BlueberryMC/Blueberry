package net.blueberrymc.common.bml.event;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.util.ThrowableConsumer;
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

    public void add(@NotNull ThrowableConsumer<@NotNull Event> consumer, @NotNull EventPriority priority, @Nullable Listener listener, @NotNull BlueberryMod mod) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(priority, "priority cannot be null");
        Preconditions.checkNotNull(mod, "mod cannot be null");
        listeners.add(new RegisteredListener(consumer, priority, listener, mod));
    }

    public void remove(@NotNull("mod") BlueberryMod mod) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        List<RegisteredListener> toRemove = new ArrayList<>();
        listeners.forEach(registeredListener -> {
            if (mod.equals(registeredListener.getMod())) {
                toRemove.add(registeredListener);
            }
        });
        toRemove.forEach(listeners::remove);
    }

    public void remove(@NotNull("listener") Listener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        List<RegisteredListener> toRemove = new ArrayList<>();
        listeners.forEach(registeredListener -> {
            if (listener.equals(registeredListener.getListener())) {
                toRemove.add(registeredListener);
            }
        });
        toRemove.forEach(listeners::remove);
    }

    public void fire(@NotNull("event") Event event) {
        Preconditions.checkNotNull(event, "event cannot be null");
        this.listeners
                .stream()
                .sorted(Comparator.comparingInt(registeredListener -> registeredListener.getPriority().getSlot()))
                .forEach(registeredListener -> {
                    try {
                        registeredListener.getExecutor().accept(event);
                    } catch (Throwable e) {
                        Throwable cause = e.getCause() != null ? e.getCause() : e.getCause();
                        String listenerName = registeredListener.getListener() == null ? null : registeredListener.getListener().getClass().getCanonicalName();
                        new EventException("Could not pass event " + event.getEventName() + " to listener " + listenerName + " of mod " + registeredListener.getMod().getName(), cause).printStackTrace();
                    }
                });
    }
}
