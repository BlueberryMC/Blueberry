package net.blueberrymc.mixin.server;

import net.blueberrymc.common.event.lifecycle.RegistryBootstrappedEvent;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Bootstrap.class, remap = false)
public class MixinBootstrap {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;bootStrap()V"), method = "bootStrap")
    private static void fireRegistryBootstrappedEvent(CallbackInfo ci) {
        RegistryBootstrappedEvent.fire();
    }
}
