package net.blueberrymc.client.event.render;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when the client tries to render a liquid. You can change the color of a liquid in this event. This event may
 * be called from both main thread and worker thread. Use {@link Event#isAsynchronous()} to distinguish between them.
 */
public class LiquidBlockRenderEvent extends RenderEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final FluidState fluidState;
    private final BlockPos blockPos;
    private int color;

    public LiquidBlockRenderEvent(@NotNull FluidState fluidState, @NotNull BlockPos blockPos, int color) {
        super(!Blueberry.getUtil().isOnGameThread());
        this.fluidState = fluidState;
        this.blockPos = blockPos;
        this.color = color;
    }

    @NotNull
    public FluidState getFluidState() {
        return fluidState;
    }

    @NotNull
    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * Get the liquid color being rendered.
     * @return the liquid color
     */
    public int getColor() {
        return color;
    }

    /**
     * Set the liquid color being rendered.
     * @param color the liquid color
     */
    public void setColor(int color) {
        this.color = color;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
