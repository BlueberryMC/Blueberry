package net.blueberrymc.common.bml;

import net.blueberrymc.client.event.render.LiquidBlockRenderEvent;
import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.EventHandler;
import net.blueberrymc.common.bml.event.Listener;
import net.blueberrymc.world.level.material.MilkFluid;

public class InternalBlueberryModListener implements Listener {
    private final InternalBlueberryMod mod;

    InternalBlueberryModListener(InternalBlueberryMod mod) {
        this.mod = mod;
    }

    @EventHandler
    public static void onLiquidBlockRender(LiquidBlockRenderEvent e) {
        if (InternalBlueberryMod.liquidMilk && e.getFluidState().getType().isSame(MilkFluid.Source.INSTANCE)) {
            e.setColor(0xFFFFFF);
        }
    }

    @EventHandler
    public void onScreenChanged(ScreenChangedEvent e) {
        if (Blueberry.getCurrentState() != ModState.AVAILABLE) return;
        this.mod.refreshDiscordStatus(e.getScreen());
    }
}
