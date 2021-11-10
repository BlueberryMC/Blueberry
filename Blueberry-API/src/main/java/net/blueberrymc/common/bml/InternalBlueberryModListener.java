package net.blueberrymc.common.bml;

import io.netty.channel.Channel;
import net.blueberrymc.client.event.render.gui.ScreenChangedEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.EventHandler;
import net.blueberrymc.common.bml.event.Listener;
import net.blueberrymc.common.event.network.ConnectionInitEvent;
import net.blueberrymc.common.event.network.ConnectionSetupCompressionEvent;
import net.blueberrymc.network.transformer.InboundPacketTransformer;
import net.blueberrymc.network.transformer.OutboundPacketTransformer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record InternalBlueberryModListener(@NotNull InternalBlueberryMod mod) {
    @Contract(value = " -> new", pure = true)
    @NotNull
    public Server createServer() {
        return new Server();
    }

    @Contract(value = " -> new", pure = true)
    @NotNull
    public Client createClient() {
        return new Client();
    }

    public class Server implements Listener {
    }

    public class Client implements Listener {
        @EventHandler
        public void onScreenChanged(@NotNull ScreenChangedEvent e) {
            if (Blueberry.getCurrentState() != ModState.AVAILABLE) return;
            InternalBlueberryModListener.this.mod.refreshDiscordStatus(e.getScreen());
        }

        @EventHandler
        public void onConnectionInit(@NotNull ConnectionInitEvent e) {
            if (e.isLocalServer()) return;
            setupTransformer(e.getChannel());
        }

        @EventHandler
        public void onConnectionSetupCompression(@NotNull ConnectionSetupCompressionEvent e) {
            if (e.isLocalServer()) return;
            setupTransformer(e.getChannel());
        }

        private void setupTransformer(Channel channel) {
            if (channel.pipeline().get("inbound_transformer") instanceof InboundPacketTransformer) {
                channel.pipeline().remove("inbound_transformer");
            }
            if (channel.pipeline().get("outbound_transformer") instanceof OutboundPacketTransformer) {
                channel.pipeline().remove("outbound_transformer");
            }
            channel.pipeline()
                    .addBefore("decoder", "inbound_transformer", new InboundPacketTransformer())
                    .addBefore("encoder", "outbound_transformer", new OutboundPacketTransformer());
        }
    }
}
