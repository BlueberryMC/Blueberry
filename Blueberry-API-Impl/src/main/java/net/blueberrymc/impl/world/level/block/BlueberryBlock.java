package net.blueberrymc.impl.world.level.block;

import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.impl.util.BlueberryRandomSource;
import net.blueberrymc.impl.util.PositionUtil;
import net.blueberrymc.impl.world.BlueberryWorld;
import net.blueberrymc.impl.world.entity.BlueberryEntity;
import net.blueberrymc.impl.world.item.BlueberryItemStack;
import net.blueberrymc.impl.world.level.block.entity.BlueberryBlockEntity;
import net.blueberrymc.impl.world.level.block.state.BlueberryBlockState;
import net.blueberrymc.util.Reflected;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.World;
import net.blueberrymc.world.entity.Entity;
import net.blueberrymc.world.item.ItemStack;
import net.blueberrymc.world.level.block.BlockFace;
import net.blueberrymc.world.level.block.entity.BlockEntity;
import net.blueberrymc.world.level.block.state.BlockState;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlueberryBlock {
    @SuppressWarnings("DuplicatedCode")
    @Reflected
    public static @NotNull List<@NotNull ItemStack> getDrops(@NotNull BlockState state, @NotNull World world, @NotNull Vec3i pos, @Nullable BlockEntity blockEntity) {
        var level = ((BlueberryWorld) world).handle();
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("Cannot get drops from client-side world");
        }
        var mcState = ((BlueberryBlockState) state).handle();
        var mcPos = PositionUtil.toBlockPos(pos);
        var mcBlockEntity = Util.mapNullable(blockEntity, be -> ((BlueberryBlockEntity) be).handle());
        return Block.getDrops(mcState, serverLevel, mcPos, mcBlockEntity)
                .stream()
                .map((Function<net.minecraft.world.item.ItemStack, ItemStack>) BlueberryItemStack::new)
                .toList();
    }

    @SuppressWarnings("DuplicatedCode")
    @Reflected
    public static @NotNull List<@NotNull ItemStack> getDrops(@NotNull BlockState state, @NotNull World world, @NotNull Vec3i pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, @NotNull ItemStack itemStack) {
        var level = ((BlueberryWorld) world).handle();
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("Cannot get drops from client-side world");
        }
        var mcState = ((BlueberryBlockState) state).handle();
        var mcPos = PositionUtil.toBlockPos(pos);
        var mcBlockEntity = Util.mapNullable(blockEntity, be -> ((BlueberryBlockEntity) be).handle());
        var mcEntity = Util.mapNullable(entity, BlueberryEntity::toMinecraft);
        var mcItemStack = BlueberryItemStack.toMinecraft(itemStack);
        return Block.getDrops(mcState, serverLevel, mcPos, mcBlockEntity, mcEntity, mcItemStack)
                .stream()
                .map((Function<net.minecraft.world.item.ItemStack, ItemStack>) BlueberryItemStack::new)
                .toList();
    }

    @Reflected
    public static void dropResources(@NotNull BlockState state, @Nullable World world, @NotNull Vec3i pos) {
        var level = Util.mapNullable(world, w -> ((BlueberryWorld) w).handle());
        var mcState = ((BlueberryBlockState) state).handle();
        var mcPos = PositionUtil.toBlockPos(pos);
        Block.dropResources(mcState, level, mcPos);
    }

    @Reflected
    public static void dropResources(@NotNull BlockState state, @Nullable World world, @NotNull Vec3i pos, @Nullable BlockEntity blockEntity) {
        var level = Util.mapNullable(world, w -> ((BlueberryWorld) w).handle());
        var mcState = ((BlueberryBlockState) state).handle();
        var mcPos = PositionUtil.toBlockPos(pos);
        var mcBlockEntity = Util.mapNullable(blockEntity, be -> ((BlueberryBlockEntity) be).handle());
        Block.dropResources(mcState, level, mcPos, mcBlockEntity);
    }

    @Reflected
    public static void dropResources(@NotNull BlockState state, @Nullable World world, @NotNull Vec3i pos, @Nullable BlockEntity blockEntity, @NotNull Entity entity, @NotNull ItemStack itemStack) {
        var level = Util.mapNullable(world, w -> ((BlueberryWorld) w).handle());
        var mcState = ((BlueberryBlockState) state).handle();
        var mcPos = PositionUtil.toBlockPos(pos);
        var mcBlockEntity = Util.mapNullable(blockEntity, be -> ((BlueberryBlockEntity) be).handle());
        var mcEntity = BlueberryEntity.toMinecraft(entity);
        var mcItemStack = BlueberryItemStack.toMinecraft(itemStack);
        Block.dropResources(mcState, level, mcPos, mcBlockEntity, mcEntity, mcItemStack);
    }

    @Reflected
    public static void popResource(@NotNull World world, @NotNull Vec3i pos, @NotNull ItemStack itemStack) {
        if (world.isClientSide()) {
            return;
        }
        float divHeight = EntityType.ITEM.getHeight() / 2.0F;
        var random = BlueberryRandomSource.of(world.getRandom());
        double x = (double) ((float) pos.x() + 0.5F) + Mth.nextDouble(random, -0.25D, 0.25D);
        double y = (double) ((float) pos.y() + 0.5F) + Mth.nextDouble(random, -0.25D, 0.25D) - (double) divHeight;
        double z = (double) ((float) pos.z() + 0.5F) + Mth.nextDouble(random, -0.25D, 0.25D);
        popResource(world, () -> new ItemEntity(((BlueberryWorld) world).handle(), x, y, z, BlueberryItemStack.toMinecraft(itemStack)), itemStack);
    }

    @Reflected
    public static void popResourceFromFace(@NotNull World world, @NotNull Vec3i pos, @NotNull BlockFace direction, @NotNull ItemStack itemStack) {
        if (world.isClientSide()) {
            return;
        }
        int modX = direction.getModX();
        int modY = direction.getModY();
        int modZ = direction.getModZ();
        float divWidth = EntityType.ITEM.getWidth() / 2.0F;
        float divHeight = EntityType.ITEM.getHeight() / 2.0F;
        var random = BlueberryRandomSource.of(world.getRandom());
        double d = (double) ((float) pos.x() + 0.5F) + (modX == 0 ? Mth.nextDouble(random, -0.25D, 0.25D) : (double) ((float) modX * (0.5F + divWidth)));
        double d2 = (double) ((float) pos.y() + 0.5F) + (modY == 0 ? Mth.nextDouble(random, -0.25D, 0.25D) : (double) ((float) modY * (0.5F + divHeight))) - (double) divHeight;
        double d3 = (double) ((float) pos.z() + 0.5F) + (modZ == 0 ? Mth.nextDouble(random, -0.25D, 0.25D) : (double) ((float) modZ * (0.5F + divWidth)));
        double d4 = modX == 0 ? Mth.nextDouble(random, -0.1D, 0.1D) : (double) modX * 0.1D;
        double d5 = modY == 0 ? Mth.nextDouble(random, 0.0D, 0.1D) : (double) modY * 0.1D + 0.1D;
        double d6 = modZ == 0 ? Mth.nextDouble(random, -0.1D, 0.1D) : (double) modZ * 0.1D;
        popResource(world, () -> new ItemEntity(((BlueberryWorld) world).handle(), d, d2, d3, BlueberryItemStack.toMinecraft(itemStack), d4, d5, d6), itemStack);
    }

    private static void popResource(@NotNull World world, Supplier<ItemEntity> supplier, @NotNull ItemStack itemStack) {
        Method m = ReflectionHelper.findMethod(Block.class, "popResource", Level.class, Supplier.class, net.minecraft.world.item.ItemStack.class);
        if (m == null) {
            throw new IllegalStateException("Could not find popResource method");
        }
        m.setAccessible(true);
        try {
            // -> private static void popResource(Level, Supplier<ItemEntity>, ItemStack)
            m.invoke(null, ((BlueberryWorld) world).handle(), supplier, BlueberryItemStack.toMinecraft(itemStack));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
