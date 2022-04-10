package net.blueberrymc.client.gui.screens;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base class of the screen.
 */
public abstract class BlueberryScreen extends Screen {
    protected BlueberryScreen(@NotNull Component title) {
        super(title);
    }

    /**
     * Gets the children of this screen.
     * @return children
     */
    @SuppressWarnings("unchecked")
    @Override
    public @NotNull List<GuiEventListener> children() {
        return (List<GuiEventListener>) super.children();
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
        this.children().forEach(listener -> listener.mouseMoved(x, y));
    }
}
