package net.blueberrymc.world.level.material;

import net.blueberrymc.registry.BlueberryRegistries;
import net.blueberrymc.tags.FluidTags;
import net.blueberrymc.tags.TagKey;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.World;
import net.blueberrymc.world.item.Item;
import net.blueberrymc.world.level.BlockGetter;
import net.blueberrymc.world.level.LevelAccessor;
import net.blueberrymc.world.level.block.Block;
import net.blueberrymc.world.level.block.BlockFace;
import net.blueberrymc.world.level.block.LiquidBlock;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import net.blueberrymc.world.level.block.state.StateDefinition;
import net.blueberrymc.world.level.fluid.FlowingFluid;
import net.blueberrymc.world.level.fluid.Fluid;
import net.blueberrymc.world.level.fluid.state.FluidState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public abstract class MilkFluid extends FlowingFluid {
    @NotNull
    public Fluid getFlowing() {
        return Flowing.INSTANCE;
    }

    @NotNull
    public Fluid getSource() {
        return Source.INSTANCE;
    }

    @NotNull
    public Item getBucket() {
        return Objects.requireNonNull(BlueberryRegistries.ITEM.get("blueberry", "milk_bucket"));
    }

    @Override
    public void animateTick(@NotNull World world, @NotNull Vec3i pos, @NotNull FluidState state, @NotNull Random random) {
        if (!state.isSource() && !state.getValue(FALLING)) {
            if (random.nextInt(64) == 0) {
                world.playLocalSound((double) pos.x() + 0.5D, (double) pos.y() + 0.5D, (double) pos.z() + 0.5D, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
            }
        } else if (random.nextInt(10) == 0) {
            world.addParticle(ParticleTypes.UNDERWATER, (double) pos.x() + random.nextDouble(), (double) pos.y() + random.nextDouble(), (double) pos.z() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    public ParticleOptions getDripParticle() {
        return ParticleTypes.DRIPPING_WATER;
    }

    protected boolean canConvertToSource() {
        return true;
    }

    protected void beforeDestroyingBlock(@NotNull LevelAccessor levelAccessor, @NotNull Vec3i pos, @NotNull BlockState blockState) {
        BlockEntity blockEntity = blockState.hasBlockEntity() ? levelAccessor.getBlockEntity(pos) : null;
        Block.dropResources(blockState, (World) levelAccessor, pos, blockEntity);
    }

    @Override
    public int getSlopeFindDistance(@NotNull World world) {
        return 4;
    }

    @NotNull
    public BlockState createLegacyBlock(@NotNull FluidState fluidState) {
        return Objects.requireNonNull(BlueberryRegistries.BLOCK.get("blueberry", "milk"))
                .defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState));
    }

    @Override
    public boolean isSame(@NotNull Fluid fluid) {
        return fluid == Source.INSTANCE || fluid == Flowing.INSTANCE;
    }

    @Override
    public int getDropOff(@NotNull World world) {
        return 1;
    }

    @Override
    public int getTickDelay(@NotNull World world) {
        return 5;
    }

    @Override
    public boolean canBeReplacedWith(@NotNull FluidState fluidState, @NotNull BlockGetter blockGetter, @NotNull Vec3i pos, @NotNull Fluid fluid, @NotNull BlockFace face) {
        // TODO: it uses deprecated method
        return face == BlockFace.DOWN && !fluid.is(FluidTags.WATER);
    }

    protected float getExplosionResistance() {
        return 100.0F;
    }

    public static class Flowing extends MilkFluid {
        public static final Flowing INSTANCE = new Flowing();

        protected void createFluidStateDefinition(@NotNull StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
      protected boolean canConvertToSource(Level level) {
         return true;
      }public int getAmount(@NotNull FluidState fluidState) {
            return fluidState.getValue(LEVEL);
        }

        public boolean isSource(@NotNull FluidState fluidState) {
            return false;
        }

        @Override
        public boolean is(@NotNull TagKey<Fluid> tagKey) {
            return tagKey == FluidTags.WATER;
        }
    }

    public static class Source extends MilkFluid {
        public static final Source INSTANCE = new Source();

        @Override
      protected boolean canConvertToSource(Level level) {
         return false;
      }public int getAmount(@NotNull FluidState fluidState) {
            return 8;
        }

        public boolean isSource(@NotNull FluidState fluidState) {
            return true;
        }


        @Override
        public boolean is(@NotNull TagKey<Fluid> tagKey) {
            return tagKey == FluidTags.WATER;
        }
    }
}