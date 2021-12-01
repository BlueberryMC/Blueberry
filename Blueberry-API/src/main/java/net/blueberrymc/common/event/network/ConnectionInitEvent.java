package net.blueberrymc.common.event.network;

import io.netty.channel.Channel;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the channel is active and is ready to send/receive packets. No player information is available at this
 * point. Protocol is set to {@link net.minecraft.network.ConnectionProtocol#HANDSHAKING} when the event is fired.
 */
public class ConnectionInitEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    protected final Channel channel;
    protected final PacketFlow flow;

    public ConnectionInitEvent(@NotNull Channel channel, @NotNull PacketFlow flow) {
        super(true);
        this.channel = channel;
        this.flow = flow;
    }

    public boolean isLocalServer() {
        return !channel.pipeline().toMap().containsKey("decoder");
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }

    @NotNull
    public PacketFlow getFlow() {
        return flow;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
