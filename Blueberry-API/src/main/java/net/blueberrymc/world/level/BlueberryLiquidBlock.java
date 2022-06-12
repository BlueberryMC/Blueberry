package net.blueberrymc.world.level;

import net.blueberrymc.world.level.block.LiquidBlock;
import net.blueberrymc.world.level.fluid.FlowingFluid;
import org.jetbrains.annotations.NotNull;

/**
 * LiquidBlock without any special features
 */
public class BlueberryLiquidBlock extends LiquidBlock {
    public BlueberryLiquidBlock(@NotNull FlowingFluid flowingFluid, @NotNull Properties properties) {
        super(flowingFluid, properties);
    }
}
