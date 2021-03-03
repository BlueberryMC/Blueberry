package net.blueberrymc.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface MinecraftBlockEntityRenderDispatcher {
    void registerSpecialRenderer(BlockEntityType<?> blockEntityType, BlockEntityRenderer<?> blockEntityRenderer);
}
