package net.blueberrymc.client.event.render.gui;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.minecraft.client.gui.screens.Overlay;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when the overlay was changed via {@link net.minecraft.client.Minecraft#setOverlay(Overlay)}. This event may be
 * called from any thread. Use {@link Event#isAsynchronous()} to distinguish between them. This event is not fired
 * when the overlay was changed via reflection etc.
 */
public class OverlayChangedEvent extends Event {
    protected final Overlay overlay;

    public OverlayChangedEvent(@Nullable Overlay overlay) {
        this(overlay, !Blueberry.getUtil().isOnGameThread());
    }

    private OverlayChangedEvent(@Nullable Overlay overlay, boolean async) {
        super(async);
        this.overlay = overlay;
    }

    /**
     * Gets the new overlay.
     * @return the overlay
     */
    @Nullable
    public Overlay getOverlay() {
        return overlay;
    }
}
