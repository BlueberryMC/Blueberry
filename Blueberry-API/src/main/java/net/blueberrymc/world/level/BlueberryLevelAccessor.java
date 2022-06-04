package net.blueberrymc.world.level;

import com.google.common.base.Preconditions;
import net.blueberrymc.util.Vec3;
import net.blueberrymc.util.Vec3i;
import net.blueberrymc.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public interface BlueberryLevelAccessor extends LevelAccessor {
    @Contract("_, _, _, _ -> new")
    @NotNull
    static Vec3 randomLocationWithinBlock(@NotNull Vec3i pos, double xs, double ys, double zs) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        return randomLocationWithinBlock(pos.toVec3(), xs, ys, zs);
    }

    @Contract("_, _, _, _ -> param1")
    @NotNull
    static Vec3 randomLocationWithinBlock(@NotNull Vec3 pos, double xs, double ys, double zs) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        final double prevX = pos.x();
        final double prevY = pos.y();
        final double prevZ = pos.z();
        double currX = pos.x();
        double currY = pos.y();
        double currZ = pos.z();
        currX += xs;
        currY += ys;
        currZ += zs;
        if (currX < Math.floor(prevX)) {
            currX = Math.floor(prevX);
        }
        if (currX >= Math.ceil(prevX)) {
            currX = Math.ceil(prevX - 0.01D);
        }
        if (currY < Math.floor(prevY)) {
            currY = Math.floor(prevY);
        }
        if (currY >= Math.ceil(prevY)) {
            currY = Math.ceil(prevY - 0.01D);
        }
        if (currZ < Math.floor(prevZ)) {
            currZ = Math.floor(prevZ);
        }
        if (currZ >= Math.ceil(prevZ)) {
            currZ = Math.ceil(prevZ - 0.01D);
        }
        return new Vec3(currX, currY, currZ);
    }

    /*
    @NotNull
    default ItemEntity dropItem(@NotNull Vec3i pos, @NotNull ItemStack item) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        return dropItem(pos.toBlockVec3(), item);
    }

    @NotNull
    default ItemEntity dropItem(@NotNull Vec3 pos, @NotNull ItemStack item) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        ItemEntity entity = new ItemEntity(getLevel(), pos.x(), pos.y(), pos.z(), item);
        entity.setPickUpDelay(10);
        this.addFreshEntity(entity);
        return entity;
    }

    @NotNull
    default ItemEntity dropItemNaturally(@NotNull Vec3i pos, @NotNull ItemStack item) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        return dropItemNaturally(pos.toBlockVec3(), item);
    }

    @NotNull
    default ItemEntity dropItemNaturally(@NotNull Vec3 pos, @NotNull ItemStack item) {
        Preconditions.checkNotNull(pos, "blockPos cannot be null");
        double xs = (double) (this.getRandom().nextFloat() * 0.7F) - 0.35D;
        double ys = (double) (this.getRandom().nextFloat() * 0.7F) - 0.35D;
        double zs = (double) (this.getRandom().nextFloat() * 0.7F) - 0.35D;
        return dropItem(randomLocationWithinBlock(pos, xs, ys, zs), item);
    }
    */

    @SuppressWarnings("NullableProblems")
    @NotNull
    default World getLevel() {
        return (World) this;
    }
}
