package net.blueberrymc.world.level.material;

import net.blueberrymc.registry.BlueberryRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public abstract class MilkFluid extends FlowingFluid {
   public Fluid getFlowing() {
      return Flowing.INSTANCE;
   }

   public Fluid getSource() {
      return Source.INSTANCE;
   }

   public Item getBucket() {
      return BlueberryRegistries.ITEM.get("blueberry", "milk_bucket");
   }

   public void animateTick(Level level, BlockPos blockPos, FluidState fluidState, Random random) {
      if (!fluidState.isSource() && !fluidState.getValue(FALLING)) {
         if (random.nextInt(64) == 0) {
            level.playLocalSound((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
         }
      } else if (random.nextInt(10) == 0) {
         level.addParticle(ParticleTypes.UNDERWATER, (double)blockPos.getX() + random.nextDouble(), (double)blockPos.getY() + random.nextDouble(), (double)blockPos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
      }
   }

   @Nullable
   public ParticleOptions getDripParticle() {
      return ParticleTypes.DRIPPING_WATER;
   }

   protected boolean canConvertToSource() {
      return true;
   }

   protected void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
      BlockEntity blockEntity = blockState.hasBlockEntity() ? levelAccessor.getBlockEntity(blockPos) : null;
      Block.dropResources(blockState, levelAccessor, blockPos, blockEntity);
   }

   public int getSlopeFindDistance(LevelReader levelReader) {
      return 4;
   }

   public BlockState createLegacyBlock(FluidState fluidState) {
      return Objects.requireNonNull(BlueberryRegistries.BLOCK.get("blueberry", "milk"))
              .defaultBlockState()
              .setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState));
   }

   public boolean isSame(Fluid fluid) {
      return fluid == Source.INSTANCE || fluid == Flowing.INSTANCE;
   }

   public int getDropOff(LevelReader levelReader) {
      return 1;
   }

   public int getTickDelay(LevelReader levelReader) {
      return 5;
   }

   public boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
      return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
   }

   protected float getExplosionResistance() {
      return 100.0F;
   }

   public static class Flowing extends MilkFluid {
      public static final Flowing INSTANCE = new Flowing();

      protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
         super.createFluidStateDefinition(builder);
         builder.add(LEVEL);
      }

      public int getAmount(FluidState fluidState) {
         return fluidState.getValue(LEVEL);
      }

      public boolean isSource(FluidState fluidState) {
         return false;
      }
   }

   public static class Source extends MilkFluid {
      public static final Source INSTANCE = new Source();

      public int getAmount(FluidState fluidState) {
         return 8;
      }

      public boolean isSource(FluidState fluidState) {
         return true;
      }
   }
}