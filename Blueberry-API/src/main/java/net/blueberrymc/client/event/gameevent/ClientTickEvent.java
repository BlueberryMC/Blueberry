package net.blueberrymc.client.event.gameevent;

import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * <code>ClientTickEvent</code> is called when the main thread of the client ticks (post-tick, pre-keyboard).
 */
public class ClientTickEvent extends Event {
    public static final ClientTickEvent INSTANCE = new ClientTickEvent();
    private static final HandlerList handlerList = new HandlerList();

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
