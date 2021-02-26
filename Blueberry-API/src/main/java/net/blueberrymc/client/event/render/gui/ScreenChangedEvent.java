package net.blueberrymc.client.event.render.gui;

import net.blueberrymc.client.event.AsyncEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

@AsyncEvent
public class ScreenChangedEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    protected final Screen screen;

    public ScreenChangedEvent(@Nullable Screen screen) {
        this(screen, !Blueberry.getUtil().isOnGameThread());
    }

    private ScreenChangedEvent(@Nullable Screen screen, boolean async) {
        super(async);
        this.screen = screen;
    }

    @Nullable
    public Screen getScreen() {
        return screen;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
