package net.blueberrymc.mixin.client.renderer.block;

import net.blueberrymc.client.world.level.fluid.FluidSpriteManager;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderDispatcher.class, remap = true)
public class MixinBlockRenderDispatcher {
    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void setupSprites(ResourceManager resourceManager, CallbackInfo ci) {
        FluidSpriteManager.setupSprites();
    }
}
