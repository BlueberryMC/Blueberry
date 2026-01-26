package net.blueberrymc.mixin.client.multiplayer;

import net.blueberrymc.network.client.ClientBlueberryPacketListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class, remap = true)
public class MixinClientPacketListener implements ClientBlueberryPacketListener {
    @Inject(at = @At("HEAD"), method = "handleCustomPayload", cancellable = true)
    public void handleCustomPayload(CustomPacketPayload customPacketPayload, CallbackInfo ci) {
        if (handleBlueberryCustomPayload(customPacketPayload)) ci.cancel();
    }
}
