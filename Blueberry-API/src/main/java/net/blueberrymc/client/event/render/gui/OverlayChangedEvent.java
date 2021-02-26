package net.blueberrymc.client.event.render.gui;

import net.blueberrymc.client.event.AsyncEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.client.gui.screens.Overlay;
import org.jetbrains.annotations.Nullable;

@AsyncEvent
public class OverlayChangedEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    protected final Overlay overlay;

    public OverlayChangedEvent(@Nullable Overlay overlay) {
        this(overlay, !Blueberry.getUtil().isOnGameThread());
    }

    private OverlayChangedEvent(@Nullable Overlay overlay, boolean async) {
        super(async);
        this.overlay = overlay;
    }

    @Nullable
    public Overlay getOverlay() {
        return overlay;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
