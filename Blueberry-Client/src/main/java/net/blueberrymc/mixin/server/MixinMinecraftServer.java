package net.blueberrymc.mixin.server;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(at = @At("RETURN"), method = "getServerModName", cancellable = true, remap = false)
    public void getServerModName(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("blueberry");
    }
}
