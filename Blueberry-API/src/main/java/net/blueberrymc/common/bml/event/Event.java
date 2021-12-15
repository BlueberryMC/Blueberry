package net.blueberrymc.common.bml.event;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event.
 * All events require a static method named getHandlerList() which returns the {@link HandlerList}.
 * @see EventManager#callEvent(Event)
 * @see Event#callEvent()
 * @see EventManager#registerEvents(BlueberryMod,Listener)
 */
public abstract class Event {
    private String name;
    private final boolean async;

    protected Event() {
        this(false);
    }

    protected Event(boolean async) {
        this.async = async;
    }

    /**
     * Calls the event and tests if cancelled.
     *
     * @return false if event was cancelled, if cancellable. true otherwise.
     */
    public boolean callEvent() {
        Blueberry.getEventManager().callEvent(this);
        if (this instanceof Cancellable) {
            return !((Cancellable) this).isCancelled();
        } else {
            return true;
        }
    }

    /**
     * Calls the event and returns this.
     * @param <T> the event type
     * @param event the event to call
     * @throws IllegalStateException when an event is fired from wrong thread.
     * @return the event
     */
    @Contract("_ -> param1")
    @NotNull
    public static <T extends Event> T callEvent(@NotNull T event) {
        return Blueberry.getEventManager().callEvent(event);
    }

    /**
     * Checks if this event is asynchronous (not on main thread). The event call will fail on these cases:
     * <ul>
     *     <li>the event is <b>asynchronous</b> but the event is fired synchronously.</li>
     *     <li>the event is <b>synchronous</b> but the event is fired asynchronously.</li>
     * </ul>
     * @return whether the event is asynchronous
     */
    public final boolean isAsynchronous() {
        return async;
    }

    @NotNull
    public final String getEventName() {
        if (name == null) {
            name = getClass().getSimpleName();
        }
        return name;
    }

    @NotNull
    public final String getEventTypeName() {
        return getClass().getTypeName();
    }
}
