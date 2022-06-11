package net.blueberrymc.common.event.network;

import io.netty.channel.Channel;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.network.BlueberryPacketFlow;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the compression level was set by the server.
 */
public class ConnectionSetupCompressionEvent extends Event {
    protected final Channel channel;
    protected final BlueberryPacketFlow flow;
    protected final int compressionThreshold;

    public ConnectionSetupCompressionEvent(@NotNull Channel channel, @NotNull BlueberryPacketFlow flow, int compressionThreshold) {
        super(true);
        this.channel = channel;
        this.flow = flow;
        this.compressionThreshold = compressionThreshold;
    }

    public boolean isLocalServer() {
        return !channel.pipeline().toMap().containsKey("decoder");
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }

    @NotNull
    public BlueberryPacketFlow getFlow() {
        return flow;
    }

    public int getCompressionThreshold() {
        return compressionThreshold;
    }
}
