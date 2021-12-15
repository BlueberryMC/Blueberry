package net.blueberrymc.common.bml.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.loading.ModLoadingError;
import net.blueberrymc.common.bml.loading.ModLoadingErrors;
import net.blueberrymc.common.util.ThrowableConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentHashMap<Class<? extends Event>, HandlerList> handlerMap = new ConcurrentHashMap<>();

    private void logInvalidHandler(Method method, String message, BlueberryMod mod) {
        LOGGER.warn("Invalid EventHandler: {} at {} in mod {}", message, method.toGenericString(), mod.getModId());
        ModLoadingErrors.add(new ModLoadingError(mod, String.format("Invalid EventHandler: %s at %s in mod %s", message, method.toGenericString(), mod.getModId()), true));
    }

    /**
     * Register a listener. To qualify a method for listener, the method would need to meet all these requirements:
     * <ul>
     *     <li>Parameter count is 1</li>
     *     <li>Parameter #1 is a event (like PlayerBlockBreakEvent)</li>
     *     <li>Event class must have a {@link Event valid getHandlerList() method} and public modifier</li>
     *     <li>Method is accessible from EventManager (usually method should be <code>public</code>)</li>
     *     <li>Must not be abstract</li>
     *     <li>Return type should be void (return value isn't used for now)</li>
     *     <li>Method can be static or instance method</li>
     * </ul>
     * @param mod the mod
     * @param listener the listener
     */
    public void registerEvents(@NotNull BlueberryMod mod, @NotNull Listener listener) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        for (Method method : listener.getClass().getMethods()) {
            if (method.isSynthetic()) continue;
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            EventHandler eventHandler = method.getAnnotation(EventHandler.class);
            if (method.getParameterCount() != 1) {
                logInvalidHandler(method, "parameter count is not 1", mod);
                continue;
            }
            if (!method.getReturnType().equals(void.class)) {
                logInvalidHandler(method, "warning: return type is not void (return value will not be used)", mod);
            }
            if (Modifier.isAbstract(method.getModifiers())) {
                logInvalidHandler(method, "method must not be abstract", mod);
                continue;
            }
            Class<?> clazz = method.getParameters()[0].getType();
            if (!Event.class.isAssignableFrom(clazz)) {
                logInvalidHandler(method, "parameter type is not assignable from " + Event.class.getCanonicalName(), mod);
                continue;
            }
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            if (!method.canAccess(isStatic ? null : listener)) {
                logInvalidHandler(method, "method is inaccessible from EventManager", mod);
                continue;
            }
            Class<? extends Event> eventClass = clazz.asSubclass(Event.class);
            HandlerList handlerList = getHandlerList(eventClass);
            ThrowableConsumer<Event> consumer = isStatic ? event -> method.invoke(null, event) : event -> method.invoke(listener, event);
            handlerList.add(consumer, eventHandler.priority(), listener, mod);
        }
    }

    /**
     * Register a listener.
     * @param clazz the event class
     * @param mod the mod
     * @param priority event priority
     * @param consumer event handler
     * @param <T> the event type
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void registerEvent(@NotNull Class<T> clazz, @NotNull BlueberryMod mod, @NotNull EventPriority priority, @NotNull ThrowableConsumer<T> consumer) {
        getHandlerList(clazz).add(event -> consumer.accept((T) event), priority, null, mod);
    }

    /**
     * Unregister all listeners associated with a provided mod.
     * @param mod the mod
     */
    public void unregisterEvents(@NotNull BlueberryMod mod) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        handlerMap.values().forEach(handlerList -> handlerList.remove(mod));
    }

    /**
     * Unregister a listener
     * @param listener the listener to unregister
     */
    public void unregisterEvents(@NotNull Listener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        handlerMap.values().forEach(handlerList -> handlerList.remove(listener));
    }

    /**
     * Calls an event.
     * @param event the event
     * @throws IllegalStateException when an event is fired from wrong thread.
     * @return the fired event
     */
    @Contract("_ -> param1")
    @NotNull
    public <T extends Event> T callEvent(@NotNull T event) {
        Preconditions.checkNotNull(event, "event cannot be null");
        if (Blueberry.getUtil().isOnGameThread() && event.isAsynchronous()) {
            throw new IllegalStateException(event.getEventName() + " cannot be triggered asynchronously from " + Thread.currentThread().getName());
        }
        if (!Blueberry.getUtil().isOnGameThread() && !event.isAsynchronous()) {
            throw new IllegalStateException(event.getEventName() + " cannot be triggered synchronously from " + Thread.currentThread().getName());
        }
        getHandlerList(event.getClass()).fire(event);
        return event;
    }

    /**
     * Returns the set of all events known to EventManager.
     * @return all known events
     */
    @NotNull
    public Set<Class<? extends Event>> getKnownEvents() {
        return handlerMap.keySet();
    }

    @Contract(pure = true)
    @NotNull
    @Unmodifiable
    public static Map<Class<? extends Event>, HandlerList> getHandlerMap() {
        return ImmutableMap.copyOf(handlerMap);
    }

    /**
     * Returns a handler list for an event class.
     * @param event the class of an event
     * @return handler list
     * @throws IllegalArgumentException getHandlerList method is not static
     * @throws IllegalArgumentException getHandlerList method returned null
     * @throws IllegalArgumentException getHandlerList method is inaccessible
     * @throws IllegalArgumentException getHandlerList method threw exception
     */
    @NotNull
    public static HandlerList getHandlerList(@NotNull Class<? extends Event> event) {
        Preconditions.checkNotNull(event, "event cannot be null");
        if (handlerMap.containsKey(event)) return handlerMap.get(event);
        try {
            Method method = event.getMethod("getHandlerList");
            if (!method.getReturnType().equals(HandlerList.class)) throw throwNoHandlerListError(event);
            if (!Modifier.isStatic(method.getModifiers())) throw new IllegalArgumentException("getHandlerList method on " + event.getCanonicalName() + " is not static method");
            HandlerList handlerList = (HandlerList) method.invoke(null);
            if (handlerList == null) throw new IllegalArgumentException("getHandlerList method on " + event.getCanonicalName() + " returned null");
            handlerMap.put(event, handlerList);
            return handlerList;
        } catch (NoSuchMethodException ex) {
            throw throwNoHandlerListError(event);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("getHandlerList method on " + event.getCanonicalName() + " is not accessible (make sure your method has 'public' modifier");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("getHandlerList method on " + event.getCanonicalName() + " threw exception", e.getTargetException());
        }
    }

    private static RuntimeException throwNoHandlerListError(Class<? extends Event> event) {
        return new IllegalArgumentException(event.getCanonicalName() + " does not implement a static getHandlerList() method that does not have any parameters and returns HandlerList as a result");
    }

    static void printStackTrace(@NotNull Throwable throwable) {
        LOGGER.error("", throwable);
    }
}
