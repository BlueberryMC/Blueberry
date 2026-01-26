package net.blueberrymc.mixin.client.renderer.block;

import net.blueberrymc.client.event.ClientEventFactory;
import net.blueberrymc.client.event.render.LiquidBlockRenderEvent;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LiquidBlockRenderer.class, remap = true)
public class MixinLiquidBlockRenderer {
    @ModifyVariable(at = @At(value = "STORE"), method = "tesselate", ordinal = 0)
    public int modifyLiquidColor(int value, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        LiquidBlockRenderEvent event = ClientEventFactory.callLiquidBlockRenderEvent(blockAndTintGetter, blockAndTintGetter.getFluidState(blockPos), blockPos, value);
        return event.getColor();
    }
}
