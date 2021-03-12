package net.blueberrymc.client.event.gameevent;

import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClientTickEvent extends Event {
    public static final ClientTickEvent INSTANCE = new ClientTickEvent();
    private static final HandlerList handlerList = new HandlerList();

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
