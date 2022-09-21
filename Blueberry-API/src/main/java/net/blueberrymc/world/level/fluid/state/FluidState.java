package net.blueberrymc.world.level.fluid.state;

import net.blueberrymc.common.internal.util.ImplGetter;
import net.blueberrymc.world.level.block.BlockData;
import net.blueberrymc.world.level.block.state.BlockState;
import net.blueberrymc.world.level.block.state.StateHolder;
import net.blueberrymc.world.level.fluid.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class FluidState implements StateHolder<Fluid, FluidState> {
    public boolean isSource() {
        return getOwner().isSource(this);
    }

    @Contract(pure = true)
    @NotNull
    public static FluidState create(@NotNull Fluid fluid, @NotNull Map<BlockState.Property<?>, Comparable<?>> values, @NotNull Object mapCodec) {
        return (FluidState) ImplGetter.byMethod("create", Fluid.class, Map.class, Object.class).apply(fluid, values, mapCodec);
    }
}
