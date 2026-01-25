package net.blueberrymc.mixin.client.main;

import net.blueberrymc.client.BlueberryClientImpl;
import net.blueberrymc.common.Blueberry;
import net.minecraft.SharedConstants;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Main.class, remap = false)
public class MixinMain {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;tryDetectVersion()V"), method = "main")
    private static void disableMethod1() {
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;enableDataFixerOptimizations()V"), method = "main")
    private static void disableMethod2() {
    }

    @Inject(at = @At(value = "INVOKE", target = "Ljoptsimple/OptionSet;valuesOf(Ljoptsimple/OptionSpec;)Ljava/util/List;"), method = "main")
    private static void enableBlueberry(String[] strings, CallbackInfo ci) {
        Blueberry.preBootstrap();
        Blueberry.bootstrap(new BlueberryClientImpl());
        SharedConstants.tryDetectVersion();
        SharedConstants.enableDataFixerOptimizations();
    }
}
