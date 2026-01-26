package net.blueberrymc.mixin.network.protocol.status;

import net.blueberrymc.network.client.handshake.ClientBlueberryHandshakePacketListener;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientStatusPacketListener.class)
public interface MixinClientStatusPacketListener extends ClientBlueberryHandshakePacketListener {
}
