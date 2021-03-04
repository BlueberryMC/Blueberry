package net.blueberrymc.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

public interface MinecraftBlockEntityRenderDispatcher {
    void registerSpecialRenderer(@NotNull BlockEntityType<?> blockEntityType, @NotNull BlockEntityRenderer<?> blockEntityRenderer);
}
