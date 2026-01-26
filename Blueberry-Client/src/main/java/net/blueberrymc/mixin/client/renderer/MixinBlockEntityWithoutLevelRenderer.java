package net.blueberrymc.mixin.client.renderer;

import net.blueberrymc.client.BlueberryClientImpl;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntityWithoutLevelRenderer.class, remap = true)
public class MixinBlockEntityWithoutLevelRenderer {
    @Inject(at = @At("TAIL"), method = "<init>")
    public void assignStatic(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet, CallbackInfo ci) {
        BlueberryClientImpl.blockEntityWithoutLevelRendererInstance = (BlockEntityWithoutLevelRenderer) (Object) this;
    }
}
