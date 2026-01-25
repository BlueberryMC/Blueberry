package net.blueberrymc.mixin.client.gui.components;

import net.blueberrymc.common.bml.InternalBlueberryModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatComponent.class, remap = false)
public class MixinChatComponent {
    @Inject(at = @At("RETURN"), method = "getWidth(D)I", cancellable = true)
    private static void getWidth(double d, CallbackInfoReturnable<Integer> cir) {
        int width = 320;
        if (InternalBlueberryModConfig.Misc.ChatSettings.extendedWidth) {
            width = Minecraft.getInstance().getWindow().getWidth() / 2 - 8;
        }
        cir.setReturnValue(Mth.floor(d * width));
    }

    @Inject(at = @At("RETURN"), method = "getHeight(D)I", cancellable = true)
    private static void getHeight(double d, CallbackInfoReturnable<Integer> cir) {
        int height = 180;
        if (InternalBlueberryModConfig.Misc.ChatSettings.extendedHeight) {
            height = Minecraft.getInstance().getWindow().getHeight() / 2 - 40;
        }
        cir.setReturnValue(Mth.floor(d * height));
    }
}
