package net.blueberrymc.common.bml;

import net.blueberrymc.client.event.render.LiquidBlockRenderEvent;
import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.EventHandler;
import net.blueberrymc.common.bml.event.Listener;
import net.blueberrymc.world.level.material.MilkFluid;
import org.jetbrains.annotations.NotNull;

public record InternalBlueberryModListener(@NotNull InternalBlueberryMod mod) implements Listener {
    @EventHandler
    public static void onLiquidBlockRender(@NotNull LiquidBlockRenderEvent e) {
        if (InternalBlueberryMod.liquidMilk && e.getFluidState().getType().isSame(MilkFluid.Source.INSTANCE)) {
            e.setColor(0xFFFFFF);
        }
    }

    @EventHandler
    public void onScreenChanged(@NotNull ScreenChangedEvent e) {
        if (Blueberry.getCurrentState() != ModState.AVAILABLE) return;
        this.mod.refreshDiscordStatus(e.getScreen());
    }
}
