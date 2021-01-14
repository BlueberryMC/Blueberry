package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.EventHandler;
import net.blueberrymc.common.bml.event.HandlerList;
import net.blueberrymc.common.bml.event.Listener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentHashMap<Class<? extends Event>, HandlerList> handlerMap = new ConcurrentHashMap<>();

    private void logInvalidHandler(Method method, String message) {
        LOGGER.warn("Invalid EventHandler: {} at {}", message, method.toGenericString());
    }

    public void registerEvents(@NotNull BlueberryMod mod, @NotNull Listener listener) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        Map.Entry<Listener, BlueberryMod> entry = new AbstractMap.SimpleImmutableEntry<>(listener, mod);
        for (Method method : listener.getClass().getMethods()) {
            if (method.isSynthetic()) continue;
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) {
                logInvalidHandler(method, "parameter count is not 1");
                continue;
            }
            if (!method.getReturnType().equals(void.class)) {
                logInvalidHandler(method, "return type must be void");
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                logInvalidHandler(method, "method must not be static");
                continue;
            }
            /* // TODO: do we need this check?
            if (Modifier.isAbstract(method.getModifiers())) {
                logInvalidHandler(method, "method must not be abstract");
                continue;
            }
            */
            Class<?> clazz = method.getParameters()[0].getType();
            if (!Event.class.isAssignableFrom(clazz)) {
                logInvalidHandler(method, "return type is not assignable from net.blueberrymc.common.bml.event.Event");
                continue;
            }
            Class<? extends Event> eventClass = clazz.asSubclass(Event.class);
            HandlerList handlerList = getHandlerList(eventClass);
            handlerList.add(method, entry);
        }
    }

    public void unregisterEvents(@NotNull BlueberryMod mod) {
        handlerMap.values().forEach(handlerList -> handlerList.remove(mod));
    }

    public void callEvent(@NotNull Event event) {
        Preconditions.checkNotNull(event, "event cannot be null");
        getHandlerList(event.getClass()).fire(event);
    }

    @NotNull
    public List<Class<? extends Event>> getKnownEvents() {
        return new ArrayList<>(handlerMap.keySet());
    }

    @NotNull
    public static HandlerList getHandlerList(@NotNull Class<? extends Event> event) {
        Preconditions.checkNotNull(event, "event cannot be null");
        if (handlerMap.containsKey(event)) return handlerMap.get(event);
        try {
            Method method = event.getMethod("getHandlerList");
            if (!method.getReturnType().equals(HandlerList.class)) throw throwNoHandlerListError(event);
            HandlerList handlerList = (HandlerList) method.invoke(null);
            if (handlerList == null) throw new IllegalArgumentException("getHandlerList method on " + event.getCanonicalName() + " returned null");
            handlerMap.put(event, handlerList);
            return handlerList;
        } catch (NoSuchMethodException ex) {
            throw throwNoHandlerListError(event);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("getHandlerList method on " + event.getCanonicalName() + " is not accessible");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("getHandlerList method on " + event.getCanonicalName() + " threw exception", e);
        }
    }

    private static RuntimeException throwNoHandlerListError(Class<? extends Event> event) {
        return new IllegalArgumentException(event.getCanonicalName() + " does not implement a static getHandlerList() method that returns HandlerList as a result");
    }
}
