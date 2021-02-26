package net.blueberrymc.common.bml.event;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerList {
    private final ConcurrentHashMap<Method, Map.Entry<Listener, BlueberryMod>> methods = new ConcurrentHashMap<>();

    public void add(@NotNull Method method, @NotNull Map.Entry<Listener, BlueberryMod> entry) {
        Preconditions.checkNotNull(method, "method cannot be null");
        Preconditions.checkNotNull(entry, "entry cannot be null");
        synchronized (methods) {
            if (methods.containsKey(method)) {
                throw new IllegalArgumentException("Event handler conflict: " + methods.get(method).getValue().getName() + " and " + entry.getValue().getName());
            }
            methods.put(method, entry);
        }
    }

    public void remove(@NotNull("mod") BlueberryMod mod) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        List<Method> toRemove = new ArrayList<>();
        synchronized (methods) {
            methods.forEach((method, entry) -> {
                if (entry.getValue().equals(mod)) {
                    toRemove.add(method);
                }
            });
            toRemove.forEach(methods::remove);
        }
    }

    public void remove(@NotNull("listener") Listener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        List<Method> toRemove = new ArrayList<>();
        synchronized (methods) {
            methods.forEach((method, entry) -> {
                if (entry.getKey().equals(listener)) {
                    toRemove.add(method);
                }
            });
            toRemove.forEach(methods::remove);
        }
    }

    public void fire(@NotNull("event") Event event) {
        Preconditions.checkNotNull(event, "event cannot be null");
        this.methods.forEach((method, entry) -> {
            try {
                if (Modifier.isStatic(method.getModifiers())) {
                    method.invoke(null, event);
                } else {
                    method.invoke(entry.getKey(), event);
                }
            } catch (Throwable e) {
                throw new EventException("Could not pass event " + event.getEventName() + " to listener " + entry.getKey().getClass().getCanonicalName() + " of mod " + entry.getValue().getName(), e);
            }
        });
    }
}
