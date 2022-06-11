package net.blueberrymc.world.level.fluid.state;

import net.blueberrymc.world.level.block.state.StateHolder;
import net.blueberrymc.world.level.fluid.Fluid;

public abstract class FluidState implements StateHolder<Fluid, FluidState> {
    public boolean isSource() {
        return getOwner().isSource(this);
    }
}
