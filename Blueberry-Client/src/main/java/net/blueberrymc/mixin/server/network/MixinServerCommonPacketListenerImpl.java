package net.blueberrymc.mixin.server.network;

import net.blueberrymc.network.server.ServerBlueberryPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public class MixinServerCommonPacketListenerImpl implements ServerBlueberryPacketListener {
    @Inject(at = @At("HEAD"), method = "handleCustomPayload", cancellable = true)
    public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket, CallbackInfo ci) {
        if (ServerBlueberryPacketListener.super.handleBlueberryCustomPayload(serverboundCustomPayloadPacket)) ci.cancel();
    }
}
