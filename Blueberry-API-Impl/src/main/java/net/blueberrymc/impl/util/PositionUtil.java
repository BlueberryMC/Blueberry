package net.blueberrymc.impl.util;

import net.blueberrymc.util.Vec3;
import net.blueberrymc.util.Vec3i;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class PositionUtil {
    @Contract("_ -> new")
    public static @NotNull BlockPos toBlockPos(@NotNull Vec3i vec3i) {
        return new BlockPos(vec3i.x(), vec3i.y(), vec3i.z());
    }

    @Contract("_ -> new")
    public static @NotNull Vec3i toVec3i(@NotNull BlockPos pos) {
        return new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }

    @Contract("_ -> new")
    public static @NotNull BlockPos toBlockPos(@NotNull Vec3 vec3) {
        return new BlockPos(vec3.x(), vec3.y(), vec3.z());
    }

    @Contract("_ -> new")
    public static @NotNull Vec3 toVec3(@NotNull BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    @Contract("_ -> new")
    public static @NotNull Vec3 toBlueberry(@NotNull net.minecraft.world.phys.Vec3 vec3) {
        return new Vec3(vec3.x(), vec3.y(), vec3.z());
    }

    @Contract("_ -> new")
    public static @NotNull net.minecraft.world.phys.Vec3 toMinecraft(@NotNull Vec3 vec3) {
        return new net.minecraft.world.phys.Vec3(vec3.x(), vec3.y(), vec3.z());
    }

    @Contract("_ -> new")
    public static @NotNull Vec3i toBlueberry(@NotNull net.minecraft.core.Vec3i vec3i) {
        return new Vec3i(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    @Contract("_ -> new")
    public static @NotNull net.minecraft.core.Vec3i toMinecraft(@NotNull Vec3i vec3i) {
        return new net.minecraft.core.Vec3i(vec3i.x(), vec3i.y(), vec3i.z());
    }
}
