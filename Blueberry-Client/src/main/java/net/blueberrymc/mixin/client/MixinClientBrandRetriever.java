package net.blueberrymc.mixin.client;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientBrandRetriever.class, remap = true)
public class MixinClientBrandRetriever {
    @Inject(at = @At("RETURN"), method = "getClientModName", cancellable = true, remap = false)
    private static void getClientModName(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("blueberry");
    }
}
