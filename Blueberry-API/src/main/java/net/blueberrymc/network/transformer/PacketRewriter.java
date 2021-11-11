package net.blueberrymc.network.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.blueberrymc.native_util.NativeUtil;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.blueberrymc.network.transformer.PacketRewriterManager.remapInboundPacketId;
import static net.blueberrymc.network.transformer.PacketRewriterManager.remapOutboundPacketId;

// new -> old
public class PacketRewriter {
    private final int sourcePV;
    private final int targetPV;
    private final Map<ConnectionProtocol, Map<Integer, Integer>> remapInbounds = new HashMap<>();
    private final Map<ConnectionProtocol, Map<Integer, Integer>> remapOutbounds = new HashMap<>();
    private final Map<ConnectionProtocol, Multimap<Integer, Consumer<PacketWrapper>>> rewriteInbounds = new HashMap<>();
    private final Map<ConnectionProtocol, Multimap<Integer, Consumer<PacketWrapper>>> rewriteOutbounds = new HashMap<>();
    private boolean registeringInbound;
    private boolean registeringOutbound;
    private boolean hasRegistered = false;

    /**
     * @param sourcePV source protocol version (pv before rewrite)
     * @param targetPV target protocol version (pv after rewrite)
     */
    protected PacketRewriter(int sourcePV, int targetPV) {
        this.sourcePV = sourcePV;
        this.targetPV = targetPV;
    }

    public void register() {
        if (hasRegistered) throw new IllegalStateException("PacketRewriter already registered");
        registeringInbound = true;
        preRegisterInbound();
        registerInbound();
        registeringInbound = false;
        registeringOutbound = true;
        preRegisterOutbound();
        registerOutbound();
        registeringOutbound = false;
        hasRegistered = true;
    }

    protected PacketRewriter(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        this(sourcePV.getProtocolVersion(), targetPV.getProtocolVersion());
    }

    @NotNull
    public static PacketRewriter of(int sourcePV, int targetPV) {
        return new PacketRewriter(sourcePV, targetPV);
    }

    @NotNull
    public static PacketRewriter of(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        return of(sourcePV.getProtocolVersion(), targetPV.getProtocolVersion());
    }

    public int getSourcePV() {
        return sourcePV;
    }

    public int getTargetPV() {
        return targetPV;
    }

    protected void preRegisterInbound() {
    }

    protected void preRegisterOutbound() {
        // ClientIntentionPacket (server-bound)
        // this implementation just changes the protocol version
        rewriteOutbound(ConnectionProtocol.HANDSHAKING, 0x00, wrapper -> {
            wrapper.readVarInt();
            wrapper.writeVarInt(getTargetPV());
            wrapper.passthroughAll();
        });
    }

    protected void registerSoundRewriter() {
        // ClientboundSoundEntityPacket
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x5C, targetPV), wrapper -> {
            wrapper.writeVarInt(remapSoundId(wrapper.readVarInt()));
            wrapper.passthroughAll();
        });
        // ClientboundSoundPacket
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x5D, targetPV), wrapper -> {
            wrapper.writeVarInt(remapSoundId(wrapper.readVarInt()));
            wrapper.passthroughAll();
        });
    }

    protected void registerItemRewriter() {
        internalRewrite(rewriteOutbounds, ConnectionProtocol.PLAY, remapOutboundPacketId(ConnectionProtocol.PLAY, 0x08, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ServerboundContainerClickPacket(wrapper)));
        internalRewrite(rewriteOutbounds, ConnectionProtocol.PLAY, remapOutboundPacketId(ConnectionProtocol.PLAY, 0x28, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ServerboundSetCreativeModeSlotPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x14, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundContainerSetContentPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x16, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundContainerSetSlotPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x28, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundMerchantOffersPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x4D, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundSetEntityDataPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x50, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundSetEquipmentPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x63, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundUpdateAdvancementsPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x66, targetPV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundUpdateRecipesPacket(wrapper)));
    }

    protected void registerParticleRewriter() {
        // ClientboundLevelParticlesPacket
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, 0x24, targetPV), wrapper -> {
            wrapper.writeInt(remapParticleId(wrapper.readInt()));
            wrapper.passthroughAll();
        });
    }

    protected int remapSoundId(int soundId) {
        return soundId;
    }

    protected int remapParticleId(int particleId) {
        return particleId;
    }

    @NotNull
    protected ItemStack rewriteOutboundItemData(@NotNull PacketWrapper wrapper) {
        return passthroughItemData(wrapper);
    }

    @NotNull
    protected ItemStack rewriteInboundItemData(@NotNull PacketWrapper wrapper) {
        return passthroughItemData(wrapper);
    }

    protected final ItemStack passthroughItemData(@NotNull PacketWrapper wrapper) {
        if (wrapper.passthroughBoolean()) { // present
            var id = wrapper.passthroughVarInt();
            var count = wrapper.passthroughByte();
            var tag = wrapper.passthroughNbt();
            ItemStack item = new ItemStack(Item.byId(id), count);
            item.setTag(tag);
            return item;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void registerInbound() {}

    public void registerOutbound() {}

    protected final void remapInbound(@NotNull ConnectionProtocol protocol, int newId, int oldId) {
        if (!registeringInbound) throw new IllegalStateException("Not registering inbound");
        remapInbounds.computeIfAbsent(protocol, (k) -> new HashMap<>()).put(oldId, newId);
    }

    protected final void remapOutbound(@NotNull ConnectionProtocol protocol, int newId, int oldId) {
        if (!registeringOutbound) throw new IllegalStateException("Not registering outbound");
        remapOutbounds.computeIfAbsent(protocol, (k) -> new HashMap<>()).put(oldId, newId);
    }

    /**
     * @param protocol the protocol
     * @param newId packet id of newer version
     * @return packet id of older version
     */
    public final int getInboundId(@NotNull ConnectionProtocol protocol, int newId) {
        if (!remapInbounds.containsKey(protocol)) return newId;
        return Objects.requireNonNullElse(remapInbounds.get(protocol).get(newId), newId);
    }

    /**
     * @param protocol the protocol
     * @param newId packet id of newer version
     * @return packet id of older version
     */
    public final int getOutboundId(@NotNull ConnectionProtocol protocol, int newId) {
        if (!remapOutbounds.containsKey(protocol)) return newId;
        return Objects.requireNonNullElse(remapOutbounds.get(protocol).get(newId), newId);
    }

    protected final void rewriteInbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull Consumer<PacketWrapper> handler) {
        if (!registeringInbound) throw new IllegalStateException("Not registering inbound");
        internalRewrite(rewriteInbounds, protocol, oldId, handler);
    }

    protected final void rewriteOutbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull Consumer<PacketWrapper> handler) {
        if (!registeringOutbound) throw new IllegalStateException("Not registering outbound");
        internalRewrite(rewriteOutbounds, protocol, oldId, handler);
    }

    protected final void internalRewrite(Map<ConnectionProtocol, Multimap<Integer, Consumer<PacketWrapper>>> map, ConnectionProtocol protocol, int oldId, Consumer<PacketWrapper> handler) {
        map.computeIfAbsent(protocol, (k) -> ArrayListMultimap.create(4, 3)).put(oldId, handler);
    }

    public final void doRewriteInbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper) {
        PacketWrapperRewriter packetWrapperRewriter = new PacketWrapperRewriter(wrapper, this::rewriteInboundItemData);
        doRewrite(protocol, oldId, packetWrapperRewriter, rewriteInbounds);
    }

    public final void doRewriteOutbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper) {
        PacketWrapperRewriter packetWrapperRewriter = new PacketWrapperRewriter(wrapper, this::rewriteOutboundItemData);
        doRewrite(protocol, oldId, packetWrapperRewriter, rewriteOutbounds);
    }

    private void doRewrite(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper, Map<ConnectionProtocol, Multimap<Integer, Consumer<PacketWrapper>>> map) {
        if (!map.containsKey(protocol)) {
            wrapper.passthroughAll();
            return;
        }
        Collection<Consumer<PacketWrapper>> consumers = map.get(protocol).get(oldId);
        if (consumers.isEmpty()) {
            wrapper.passthroughAll();
            return;
        }
        for (Consumer<PacketWrapper> consumer : consumers) {
            consumer.accept(wrapper);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public final Map<ResourceLocation, IntList> getTags(@NotNull TagCollection.NetworkPayload networkPayload) {
        try {
            return (Map<ResourceLocation, IntList>) NativeUtil.get(TagCollection.NetworkPayload.class.getDeclaredField("tags"), networkPayload);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public final void addEmptyTags(@NotNull TagCollection.NetworkPayload networkPayload, @NotNull ResourceLocation@NotNull... locations) {
        Map<ResourceLocation, IntList> map = getTags(networkPayload);
        for (ResourceLocation location : locations) {
            map.put(location, IntLists.emptyList());
        }
    }

    public final void addEmptyTags(@NotNull TagCollection.NetworkPayload networkPayload, @NotNull String@NotNull... locations) {
        Map<ResourceLocation, IntList> map = getTags(networkPayload);
        for (String location : locations) {
            map.put(new ResourceLocation(location), IntLists.emptyList());
        }
    }

    public static class PacketWrapperRewriter extends PacketWrapper {
        private final Function<PacketWrapper, ItemStack> rewriteItem;

        public PacketWrapperRewriter(@NotNull PacketWrapper wrapper, @NotNull Function<PacketWrapper, ItemStack> rewriteItem) {
            super(wrapper.getRead(), wrapper.getWrite());
            this.rewriteItem = rewriteItem;
        }

        @Override
        public @NotNull ItemStack passthroughItem() {
            return rewriteItem.apply(this);
        }

        @Override
        public @NotNull PacketWrapper passthrough(@NotNull Type type) {
            if (type == Type.ITEM) {
                rewriteItem.apply(this);
                return this;
            }
            return super.passthrough(type);
        }
    }
}
