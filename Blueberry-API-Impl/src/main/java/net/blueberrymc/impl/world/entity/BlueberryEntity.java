package net.blueberrymc.impl.world.entity;

import net.blueberrymc.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class BlueberryEntity implements Entity {
    private final net.minecraft.world.entity.Entity handle;

    public BlueberryEntity(@NotNull net.minecraft.world.entity.Entity handle) {
        this.handle = handle;
    }

    @NotNull
    public net.minecraft.world.entity.Entity getHandle() {
        return handle;
    }

    public static @NotNull net.minecraft.world.entity.Entity toMinecraft(@NotNull Entity entity) {
        return ((BlueberryEntity) entity).getHandle();
    }
}
