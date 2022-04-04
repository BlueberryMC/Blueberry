package net.blueberrymc.client.event.gameevent;

import net.blueberrymc.common.bml.event.Event;

/**
 * <code>ClientTickEvent</code> is called when the main thread of the client ticks (post-tick, pre-keyboard).
 */
public class ClientTickEvent extends Event {
    public static final ClientTickEvent INSTANCE = new ClientTickEvent();
}
