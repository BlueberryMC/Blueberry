package net.blueberrymc.network.transformer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.blueberrymc.common.bml.InternalBlueberryModConfig;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import org.jetbrains.annotations.NotNull;

public class OutboundPacketTransformer extends ChannelDuplexHandler {
    @Override
    public void write(@NotNull ChannelHandlerContext ctx, @NotNull Object msg, @NotNull ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            ConnectionProtocol protocol = ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get();
            int targetPV = InternalBlueberryModConfig.Multiplayer.version.getProtocolVersion();
            ByteBuf buf = PacketRewriterManager.rewriteOutbound(protocol, byteBuf, targetPV);
            super.write(ctx, buf, promise);
            return;
        }
        super.write(ctx, msg, promise);
    }
}
