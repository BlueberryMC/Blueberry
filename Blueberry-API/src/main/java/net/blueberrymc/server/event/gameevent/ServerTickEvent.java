package net.blueberrymc.server.event.gameevent;

import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ServerTickEvent extends Event {
    public static final ServerTickEvent INSTANCE = new ServerTickEvent();
    private static final HandlerList handlerList = new HandlerList();

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
