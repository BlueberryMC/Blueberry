package net.blueberrymc.common.bml.event;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerList {
    private final ConcurrentHashMap<Method, Map.Entry<Listener, BlueberryMod>> methods = new ConcurrentHashMap<>();

    public void add(Method method, Map.Entry<Listener, BlueberryMod> entry) {
        Preconditions.checkNotNull(method, "method cannot be null");
        Preconditions.checkNotNull(entry, "entry cannot be null");
        synchronized (methods) {
            if (methods.containsKey(method)) {
                throw new IllegalArgumentException("Event handler conflict: " + methods.get(method).getValue().getName() + " and " + entry.getValue().getName());
            }
            methods.put(method, entry);
        }
    }

    public void remove(@NotNull BlueberryMod mod) {
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

    public void fire(Event event) {
        this.methods.forEach((method, entry) -> {
            try {
                method.invoke(entry.getKey(), event);
            } catch (Throwable e) {
                throw new EventException("Could not pass event to listener " + entry.getKey().getClass().getCanonicalName() + " of mod " + entry.getValue().getName(), e);
            }
        });
    }
}
