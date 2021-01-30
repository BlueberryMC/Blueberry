package net.blueberrymc.network;

import io.netty.buffer.Unpooled;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.util.reflect.Ref;
import net.blueberrymc.common.util.reflect.RefField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class BlueberryNetworkManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final RefField<ServerboundCustomPayloadPacket> SERVERBOUND_CUSTOM_PAYLOAD_PACKET_REF_IDENTIFIER_FIELD = Ref.getClass(ServerboundCustomPayloadPacket.class).getDeclaredField("identifier").accessible(true);
    private static final RefField<ServerboundCustomPayloadPacket> SERVERBOUND_CUSTOM_PAYLOAD_PACKET_REF_DATA_FIELD = Ref.getClass(ServerboundCustomPayloadPacket.class).getDeclaredField("data").accessible(true);
    private static final Map<ResourceLocation, Supplier<BlueberryPacket<?>>> clientBoundPacketMap = new HashMap<>();
    private static final Map<ResourceLocation, Supplier<BlueberryPacket<?>>> serverBoundPacketMap = new HashMap<>();

    public static void register(@NotNull BlueberryMod mod, @NotNull String id, @NotNull Supplier<BlueberryPacket<?>> packetConstructor, @NotNull BlueberryPacketFlow flow) {
        register(mod.getModId(), id, packetConstructor, flow);
    }

    public static void register(@NotNull String namespace, @NotNull String id, @NotNull Supplier<BlueberryPacket<?>> packetConstructor, @NotNull BlueberryPacketFlow flow) {
        register(new ResourceLocation(namespace.toLowerCase(Locale.ROOT), id.toLowerCase(Locale.ROOT)), packetConstructor, flow);
    }

    public static void register(@NotNull ResourceLocation resourceLocation, @NotNull Supplier<BlueberryPacket<?>> packetConstructor, @NotNull BlueberryPacketFlow flow) {
        if (flow == BlueberryPacketFlow.TO_CLIENT) {
            clientBoundPacketMap.put(resourceLocation, packetConstructor);
        } else if (flow == BlueberryPacketFlow.TO_SERVER) {
            serverBoundPacketMap.put(resourceLocation, packetConstructor);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Nullable
    public static Supplier<BlueberryPacket<?>> getPacket(@NotNull BlueberryMod mod, @NotNull String id, @NotNull BlueberryPacketFlow flow) {
        return getPacket(mod.getModId(), id, flow);
    }

    @Nullable
    public static Supplier<BlueberryPacket<?>> getPacket(@NotNull String namespace, @NotNull String id, @NotNull BlueberryPacketFlow flow) {
        return getPacket(new ResourceLocation(namespace.toLowerCase(Locale.ROOT), id.toLowerCase(Locale.ROOT)), flow);
    }

    @Nullable
    public static Supplier<BlueberryPacket<?>> getPacket(@NotNull ResourceLocation resourceLocation, @NotNull BlueberryPacketFlow flow) {
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
     * @return true if handled, false otherwise
     */
    @Nullable
    public static BlueberryPacket<?> handle(@NotNull ClientboundCustomPayloadPacket packet) {
        Supplier<BlueberryPacket<?>> blueberryPacketConstructor = getPacket(packet.getIdentifier(), BlueberryPacketFlow.TO_CLIENT);
        if (blueberryPacketConstructor == null) {
            return null;
        }
        if (packet.getData() == null) return null;
        BlueberryPacket<?> blueberryPacket = null;
        try {
            blueberryPacket = blueberryPacketConstructor.get();
            blueberryPacket.read(packet.getData());
        } catch (IOException ex) {
            LOGGER.warn("Failed to handle incoming client bound packet for " + packet.getIdentifier(), ex);
        }
        packet.getData().release();
        return blueberryPacket;
    }

    /**
     * Handles the ServerboundCustomPayloadPacket.
     * @param packet the packet
     * @return true if handled, false otherwise
     */
    @Nullable
    public static BlueberryPacket<?> handle(@NotNull ServerboundCustomPayloadPacket packet) {
        ResourceLocation id = (ResourceLocation) SERVERBOUND_CUSTOM_PAYLOAD_PACKET_REF_IDENTIFIER_FIELD.get(packet);
        Supplier<BlueberryPacket<?>> blueberryPacketConstructor = getPacket(id, BlueberryPacketFlow.TO_SERVER);
        if (blueberryPacketConstructor == null) {
            return null;
        }
        FriendlyByteBuf buf = (FriendlyByteBuf) SERVERBOUND_CUSTOM_PAYLOAD_PACKET_REF_DATA_FIELD.get(packet);
        if (buf == null) return null;
        BlueberryPacket<?> blueberryPacket = null;
        try {
            blueberryPacket = blueberryPacketConstructor.get();
            blueberryPacket.read(buf);
        } catch (IOException ex) {
            LOGGER.warn("Failed to handle incoming server bound packet for " + id, ex);
        }
        buf.release();
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
        ServerboundCustomPayloadPacket customPayloadPacket = new ServerboundCustomPayloadPacket(id, buf);
        connection.send(customPayloadPacket);
    }

    public static void sendToClient(@NotNull ServerPlayer player, @NotNull BlueberryPacket<?> packet) {
        sendToClient(player.connection.connection, packet);
    }

    public static void sendToClient(@NotNull Connection connection, @NotNull BlueberryPacket<?> packet) {
        if (!connection.isConnected()) return;
        ResourceLocation id = packet.getId();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
        } catch (IOException ex) {
            LOGGER.warn("Failed to handle outgoing client bound packet for " + id, ex);
        }
        ClientboundCustomPayloadPacket customPayloadPacket = new ClientboundCustomPayloadPacket(id, buf);
        connection.send(customPayloadPacket);
    }
}
