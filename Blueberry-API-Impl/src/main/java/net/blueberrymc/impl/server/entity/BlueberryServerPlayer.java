package net.blueberrymc.impl.server.entity;

import net.blueberrymc.server.entity.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class BlueberryServerPlayer implements ServerPlayer {
    public BlueberryServerPlayer(@NotNull net.minecraft.server.level.ServerPlayer serverPlayer) {
        super(serverPlayer);
    }

    @NotNull
    @Override
    public net.minecraft.server.level.ServerPlayer getHandle() {
        return (net.minecraft.server.level.ServerPlayer) super.getHandle();
    }
}
