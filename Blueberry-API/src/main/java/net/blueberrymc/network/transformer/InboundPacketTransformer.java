package net.blueberrymc.network.transformer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.blueberrymc.common.bml.InternalBlueberryModConfig;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;

public class InboundPacketTransformer extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf byteBuf) {
            ConnectionProtocol protocol = ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get();
            int targetPV = InternalBlueberryModConfig.Multiplayer.version.getProtocolVersion();
            ByteBuf buf = PacketRewriterManager.rewriteInbound(protocol, byteBuf, targetPV);
            super.channelRead(ctx, buf);
            return;
        }
        super.channelRead(ctx, msg);
    }
}
