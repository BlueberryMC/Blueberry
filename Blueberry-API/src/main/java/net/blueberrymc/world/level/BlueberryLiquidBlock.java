package net.blueberrymc.world.level;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.NotNull;

/**
 * LiquidBlock without any special features
 */
public class BlueberryLiquidBlock extends LiquidBlock {
    public BlueberryLiquidBlock(@NotNull FlowingFluid flowingFluid, @NotNull Properties properties) {
        super(flowingFluid, properties);
    }
}
