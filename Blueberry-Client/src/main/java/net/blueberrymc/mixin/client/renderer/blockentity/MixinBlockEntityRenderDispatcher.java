package net.blueberrymc.mixin.client.renderer.blockentity;

import net.blueberrymc.client.renderer.blockentity.MinecraftBlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(value = BlockEntityRenderDispatcher.class, remap = false)
public class MixinBlockEntityRenderDispatcher implements MinecraftBlockEntityRenderDispatcher {
    @Shadow
    private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers;

    @Unique
    @Override
    public void registerSpecialRenderer(@NotNull BlockEntityType<?> blockEntityType, @NotNull BlockEntityRenderer<?> blockEntityRenderer) {
        this.renderers.put(blockEntityType, blockEntityRenderer);
    }
}
