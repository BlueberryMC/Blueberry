package net.blueberrymc.client.event.render.gui;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when the screen was changed via {@link net.minecraft.client.Minecraft#setScreen(Screen)}. This event may be
 * called from any thread. Use {@link Event#isAsynchronous()} to distinguish between them. This event is not fired
 * when the screen was changed via reflection etc.
 */
public class ScreenChangedEvent extends Event {
    protected final Screen screen;

    public ScreenChangedEvent(@Nullable Screen screen) {
        this(screen, !Blueberry.getUtil().isOnGameThread());
    }

    private ScreenChangedEvent(@Nullable Screen screen, boolean async) {
        super(async);
        this.screen = screen;
    }

    /**
     * Gets the new screen.
     * @return the screen
     */
    @Nullable
    public Screen getScreen() {
        return screen;
    }
}
