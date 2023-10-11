package net.blueberrymc.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class BlueberryNetworkManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, PacketConstructor<?>> clientBoundPacketMap = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceLocation, PacketConstructor<?>> serverBoundPacketMap = new Object2ObjectOpenHashMap<>();

    public static void register(@NotNull BlueberryMod mod, @NotNull String id, @NotNull PacketConstructor<?> packetConstructor, @NotNull BlueberryPacketFlow flow) {
        register(mod.modId(), id, packetConstructor, flow);
    }

    public static void register(@NotNull String namespace, @NotNull String id, @NotNull PacketConstructor<?> packetConstructor, @NotNull BlueberryPacketFlow flow) {
        register(new ResourceLocation(namespace.toLowerCase(Locale.ROOT), id.toLowerCase(Locale.ROOT)), packetConstructor, flow);
    }

    public static void register(@NotNull ResourceLocation resourceLocation, @NotNull PacketConstructor<?> packetConstructor, @NotNull BlueberryPacketFlow flow) {
        if (flow == BlueberryPacketFlow.TO_CLIENT) {
            clientBoundPacketMap.put(resourceLocation, packetConstructor);
        } else if (flow == BlueberryPacketFlow.TO_SERVER) {
            serverBoundPacketMap.put(resourceLocation, packetConstructor);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Nullable
    public static PacketConstructor<?> getPacket(@NotNull BlueberryMod mod, @NotNull String id, @NotNull BlueberryPacketFlow flow) {
        return getPacket(mod.modId(), id, flow);
    }

    @Nullable
    public static PacketConstructor<?> getPacket(@NotNull String namespace, @NotNull String id, @NotNull BlueberryPacketFlow flow) {
        return getPacket(new ResourceLocation(namespace.toLowerCase(Locale.ROOT), id.toLowerCase(Locale.ROOT)), flow);
    }

    @Nullable
    public static PacketConstructor<?> getPacket(@NotNull ResourceLocation resourceLocation, @NotNull BlueberryPacketFlow flow) {
        if (flow == BlueberryPacketFlow.TO_CLIENT) {
            return clientBoundPacketMap.get(resourceLocation);
        } else if (flow == BlueberryPacketFlow.TO_SERVER) {
            return serverBoundPacketMap.get(resourceLocation);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Handles the ClientboundCustomPayloadPacket.
     * @param packet the packet
     * @return non-null if created, null otherwise
     */
    @Nullable
    public static BlueberryPacket<?> handle(@NotNull CustomPacketPayload packet) {
        PacketConstructor<?> blueberryPacketConstructor = getPacket(packet.id(), BlueberryPacketFlow.TO_CLIENT);
        if (!(packet instanceof BlueberryCustomPayload customPayload)) return null;
        return createBlueberryPacket(blueberryPacketConstructor, new FriendlyByteBuf(Unpooled.wrappedBuffer(customPayload.payload())));
    }

    /**
     * Handles the ServerboundCustomPayloadPacket.
     * @param packet the packet
     * @return non-null if created, null otherwise
     */
    @Nullable
    public static BlueberryPacket<?> handle(@NotNull ServerboundCustomPayloadPacket packet) {
        PacketConstructor<?> blueberryPacketConstructor = getPacket(packet.payload().id(), BlueberryPacketFlow.TO_SERVER);
        if (!(packet.payload() instanceof BlueberryCustomPayload customPayload)) return null;
        return createBlueberryPacket(blueberryPacketConstructor, new FriendlyByteBuf(Unpooled.wrappedBuffer(customPayload.payload())));
    }

    @Nullable
    private static BlueberryPacket<?> createBlueberryPacket(PacketConstructor<?> blueberryPacketConstructor, FriendlyByteBuf data) {
        if (blueberryPacketConstructor == null) {
            return null;
        }
        BlueberryPacket<?> blueberryPacket = blueberryPacketConstructor.create(data);
        data.release();
        return blueberryPacket;
    }

    public static void sendToServer(@NotNull BlueberryPacket<?> packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        sendToServer(player.connection.getConnection(), packet);
    }

    public static void sendToServer(@NotNull Connection connection, @NotNull BlueberryPacket<?> packet) {
        if (!connection.isConnected()) return;
        ResourceLocation id = packet.getId();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
        } catch (IOException ex) {
            LOGGER.warn("Failed to handle outgoing server bound packet for " + id, ex);
        }
        ServerboundCustomPayloadPacket customPayloadPacket = new ServerboundCustomPayloadPacket(createCustomPayload(id, buf));
        connection.send(customPayloadPacket);
    }

    public static void sendToClient(@NotNull ServerPlayer player, @NotNull BlueberryPacket<?> packet) {
        sendToClient(player.connection, packet);
    }

    public static void sendToClient(@NotNull ServerGamePacketListenerImpl connection, @NotNull BlueberryPacket<?> packet) {
        if (!connection.isAcceptingMessages()) return;
        ResourceLocation id = packet.getId();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
        } catch (IOException ex) {
            LOGGER.warn("Failed to handle outgoing client bound packet for " + id, ex);
        }
        ClientboundCustomPayloadPacket customPayloadPacket = new ClientboundCustomPayloadPacket(createCustomPayload(id, buf));
        connection.send(customPayloadPacket);
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static @NotNull DiscardedPayload createCustomPayload(ResourceLocation id, ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        try {
            return DiscardedPayload.class.getConstructor(ResourceLocation.class, byte[].class).newInstance(id, bytes);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
