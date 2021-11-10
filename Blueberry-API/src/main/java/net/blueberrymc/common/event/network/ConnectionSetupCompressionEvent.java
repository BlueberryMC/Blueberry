package net.blueberrymc.common.event.network;

import io.netty.channel.Channel;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.NotNull;

public class ConnectionSetupCompressionEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    protected final Channel channel;
    protected final PacketFlow flow;
    protected final int compressionThreshold;

    public ConnectionSetupCompressionEvent(@NotNull Channel channel, @NotNull PacketFlow flow, int compressionThreshold) {
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
    public PacketFlow getFlow() {
        return flow;
    }

    public int getCompressionThreshold() {
        return compressionThreshold;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
