package net.blueberrymc.world.level.fluid;

import net.blueberrymc.tags.TagKey;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.World;
import net.blueberrymc.world.level.BlockGetter;
import net.blueberrymc.world.level.block.BlockFace;
import net.blueberrymc.world.level.block.state.StateDefinition;
import net.blueberrymc.world.level.block.state.properties.BlockStateProperties;
import net.blueberrymc.world.level.block.state.properties.BooleanProperty;
import net.blueberrymc.world.level.block.state.properties.IntegerProperty;
import net.blueberrymc.world.level.fluid.state.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public abstract class Fluid {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;

    private final StateDefinition<Fluid, FluidState> stateDefinition;

    public Fluid() {
        var builder = StateDefinition.<Fluid, FluidState>builder(this);
        createFluidStateDefinition(builder);
        this.stateDefinition = builder.create(Fluid::defaultFluidState, FluidState::new);
    }

    protected void createFluidStateDefinition(@NotNull StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
    }

    protected void animateTick(@NotNull World world, Vec3i pos, @NotNull FluidState state, @NotNull Random random) {
    }

    public abstract int getAmount(@NotNull FluidState state);

    public abstract boolean isSource(@NotNull FluidState state);

    public abstract boolean canBeReplacedWith(@NotNull FluidState state, @NotNull BlockGetter blockGetter, @NotNull Vec3i pos, @NotNull Fluid fluid, @NotNull BlockFace face);

    public abstract int getDropOff(@NotNull World world);

    public abstract int getTickDelay(@NotNull World world);

    public abstract int getSlopeFindDistance(@NotNull World world);

    public boolean isSame(@NotNull Fluid fluid) {
        return this == fluid;
    }

    public int getLegacyLevel(@NotNull FluidState state) {
        return state.getValue(LEVEL);
    }

    public abstract boolean is(@NotNull TagKey<Fluid> tagKey);
}
