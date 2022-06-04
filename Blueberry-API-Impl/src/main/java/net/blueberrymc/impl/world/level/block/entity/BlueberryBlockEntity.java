package net.blueberrymc.impl.world.level.block.entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record BlueberryBlockEntity(@NotNull BlockEntity handle) implements net.blueberrymc.world.level.block.entity.BlockEntity {
    public BlueberryBlockEntity(@NotNull Object o) {
        this((BlockEntity) o);
    }

    public BlueberryBlockEntity {
        Objects.requireNonNull(handle, "handle");
    }
}
