package net.blueberrymc.mixin.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerStatusPacketListenerImpl.class)
public class MixinServerStatusPacketListenerImpl {
    @Shadow
    @Final
    private Connection connection;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"), method = "handleStatusRequest")
    public void sendMods(ServerboundStatusRequestPacket serverboundStatusRequestPacket, CallbackInfo ci) {
        //this.connection.send(new net.blueberrymc.network.client.handshake.ClientboundBlueberryHandshakePacket(net.blueberrymc.common.Blueberry.getModLoader().getModInfos())); // Blueberry
    }
}
