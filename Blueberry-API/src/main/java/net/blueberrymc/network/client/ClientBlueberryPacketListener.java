package net.blueberrymc.network.client;

import net.blueberrymc.network.BlueberryNetworkManager;
import net.blueberrymc.network.BlueberryPacket;
import net.blueberrymc.network.BlueberryPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

public interface ClientBlueberryPacketListener extends BlueberryPacketListener {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default boolean handleBlueberryCustomPayload(ClientboundCustomPayloadPacket packet) {
        BlueberryPacket<?> blueberryPacket = BlueberryNetworkManager.handle(packet);
        if (blueberryPacket != null) {
            ((BlueberryPacket) blueberryPacket).handle(this);
            return true;
        }
        return false;
    }
}
