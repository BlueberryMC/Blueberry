package net.blueberrymc.client.renderer.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for registering special block entity renderer.
 */
public interface MinecraftBlockEntityRenderDispatcher {
    /**
     * Registers the renderer for given block entity type.
     * @param blockEntityType block entity type
     * @param blockEntityRenderer renderer
     */
    void registerSpecialRenderer(@NotNull BlockEntityType<?> blockEntityType, @NotNull BlockEntityRenderer<?> blockEntityRenderer);
}
