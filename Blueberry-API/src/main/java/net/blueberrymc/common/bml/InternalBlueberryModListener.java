package net.blueberrymc.common.bml;

import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.EventHandler;
import net.blueberrymc.common.bml.event.Listener;
import org.jetbrains.annotations.NotNull;

public record InternalBlueberryModListener(@NotNull InternalBlueberryMod mod) implements Listener {
    @EventHandler
    public void onScreenChanged(@NotNull ScreenChangedEvent e) {
        if (Blueberry.getCurrentState() != ModState.AVAILABLE) return;
        this.mod.refreshDiscordStatus(e.getScreen());
    }
}
