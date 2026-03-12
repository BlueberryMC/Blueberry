package net.blueberrymc.client.event.render;

import net.blueberrymc.common.Blueberry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @deprecated This should be replaced with a mixin-based approach. This event is no longer fired.
 */
@Deprecated
public class LiquidBlockRenderEvent extends RenderEvent {
    private final BlockAndLightGetter blockAndTintGetter;
    private final FluidState fluidState;
    private final BlockPos blockPos;
    private int color;

    public LiquidBlockRenderEvent(@NotNull BlockAndLightGetter blockAndTintGetter, @NotNull FluidState fluidState, @NotNull BlockPos blockPos, int color) {
        super(!Blueberry.getUtil().isOnGameThread()); // this event may be called from any thread because of multi-thread rendering
        Objects.requireNonNull(blockAndTintGetter, "blockAndTintGetter cannot be null");
        Objects.requireNonNull(fluidState, "fluidState cannot be null");
        Objects.requireNonNull(blockPos, "blockPos cannot be null");
        this.blockAndTintGetter = blockAndTintGetter;
        this.fluidState = fluidState;
        this.blockPos = blockPos;
        this.color = color;
    }

    /**
     * Returns the BlockAndTintGetter.
     * @return the BlockAndTintGetter
     */
    @NotNull
    public BlockAndLightGetter getBlockAndTintGetter() {
        return blockAndTintGetter;
    }

    /**
     * Get the fluid state being rendered.
     * @return the fluid state
     */
    @NotNull
    public FluidState getFluidState() {
        return fluidState;
    }

    /**
     * Get the block position being rendered.
     * @return the block position
     */
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
}
