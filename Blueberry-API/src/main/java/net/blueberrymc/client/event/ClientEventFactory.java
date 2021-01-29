package net.blueberrymc.client.event;

import net.blueberrymc.client.event.render.LiquidBlockRenderEvent;
import net.blueberrymc.common.Blueberry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public class ClientEventFactory {
    public static LiquidBlockRenderEvent callLiquidBlockRenderEvent(@NotNull FluidState fluidState, @NotNull BlockPos blockPos, int color) {
        return Blueberry.getEventManager().callEvent(new LiquidBlockRenderEvent(fluidState, blockPos, color));
    }
}
