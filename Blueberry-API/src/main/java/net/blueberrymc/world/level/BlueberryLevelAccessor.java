package net.blueberrymc.world.level;

import com.google.common.base.Preconditions;
import com.mojang.math.Vector3d;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface BlueberryLevelAccessor extends LevelAccessor {
    @Contract("_, _, _, _ -> new")
    @NotNull
    static Vector3d randomLocationWithinBlock(@NotNull BlockPos pos, double xs, double ys, double zs) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        return randomLocationWithinBlock(new Vector3d(pos.getX(), pos.getY(), pos.getZ()), xs, ys, zs);
    }

    @Contract("_, _, _, _ -> param1")
    @NotNull
    static Vector3d randomLocationWithinBlock(@NotNull Vector3d pos, double xs, double ys, double zs) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        double prevX = pos.x;
        double prevY = pos.y;
        double prevZ = pos.z;
        pos.x += xs;
        pos.y += ys;
        pos.z += zs;
        if (pos.x < Math.floor(prevX)) {
            pos.x = Math.floor(prevX);
        }
        if (pos.x >= Math.ceil(prevX)) {
            pos.x = Math.ceil(prevX - 0.01D);
        }
        if (pos.y < Math.floor(prevY)) {
            pos.y = Math.floor(prevY);
        }
        if (pos.y >= Math.ceil(prevY)) {
            pos.y = Math.ceil(prevY - 0.01D);
        }
        if (pos.z < Math.floor(prevZ)) {
            pos.z = Math.floor(prevZ);
        }
        if (pos.z >= Math.ceil(prevZ)) {
            pos.z = Math.ceil(prevZ - 0.01D);
        }
        return pos;
    }

    @NotNull
    default ItemEntity dropItem(@NotNull BlockPos pos, @NotNull ItemStack item) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        return dropItem(new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), item);
    }

    @NotNull
    default ItemEntity dropItem(@NotNull Vector3d pos, @NotNull ItemStack item) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        ItemEntity entity = new ItemEntity(getLevel(), pos.x, pos.y, pos.z, item);
        entity.setPickUpDelay(10);
        this.addFreshEntity(entity);
        return entity;
    }

    @NotNull
    default ItemEntity dropItemNaturally(@NotNull BlockPos pos, @NotNull ItemStack item) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        return dropItemNaturally(new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), item);
    }

    @NotNull
    default ItemEntity dropItemNaturally(@NotNull Vector3d pos, @NotNull ItemStack item) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        double xs = (double) (this.getRandom().nextFloat() * 0.7F) - 0.35D;
        double ys = (double) (this.getRandom().nextFloat() * 0.7F) - 0.35D;
        double zs = (double) (this.getRandom().nextFloat() * 0.7F) - 0.35D;
        return dropItem(randomLocationWithinBlock(pos, xs, ys, zs), item);
    }

    @SuppressWarnings("NullableProblems")
    @NotNull
    default Level getLevel() {
        return (Level) this;
    }
}
