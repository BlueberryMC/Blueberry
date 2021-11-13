package net.blueberrymc.network.transformer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.blueberrymc.common.bml.InternalBlueberryModConfig;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.blueberrymc.network.transformer.PacketRewriterManager.remapInboundPacketId;
import static net.blueberrymc.network.transformer.PacketRewriterManager.remapOutboundPacketId;

// new -> old
public class PacketRewriter {
    protected static final Logger LOGGER = LogManager.getLogger();
    private final int sourcePV;
    private final int targetPV;
    private final Int2ObjectMap<Int2IntMap> remapInbounds = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Int2IntMap> remapOutbounds = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Int2ObjectMap<ObjectArrayList<Consumer<PacketWrapper>>>> rewriteInbounds = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Int2ObjectMap<ObjectArrayList<Consumer<PacketWrapper>>>> rewriteOutbounds = new Int2ObjectOpenHashMap<>();
    private boolean registeringInbound;
    private boolean registeringOutbound;

    /**
     * @param sourcePV source protocol version (pv before rewrite)
     * @param targetPV target protocol version (pv after rewrite)
     */
    protected PacketRewriter(int sourcePV, int targetPV) {
        this.sourcePV = sourcePV;
        this.targetPV = targetPV;
    }

    protected void preRegister() {}

    public final void register() {
        remapInbounds.clear();
        remapOutbounds.clear();
        rewriteInbounds.clear();
        rewriteOutbounds.clear();
        preRegister();
        registeringInbound = true;
        preRegisterInbound();
        registerInbound();
        registeringInbound = false;
        registeringOutbound = true;
        preRegisterOutbound();
        registerOutbound();
        registeringOutbound = false;
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

    public int getFinalPV() {
        return InternalBlueberryModConfig.Multiplayer.version.getProtocolVersion();
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
        registerSoundRewriter(0x5C, 0x5D);
    }

    protected void registerSoundRewriter(int soundEntityPacketId, int soundPacketId) {
        // ClientboundSoundEntityPacket
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, soundEntityPacketId, sourcePV), wrapper -> {
            wrapper.writeVarInt(remapSoundId(wrapper.readVarInt()));
            wrapper.passthroughAll();
        });
        // ClientboundSoundPacket
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, soundPacketId, sourcePV), wrapper -> {
            wrapper.writeVarInt(remapSoundId(wrapper.readVarInt()));
            wrapper.passthroughAll();
        });
    }

    protected void registerItemRewriter() {
        registerItemRewriter(0x08, 0x28, 0x14, 0x16, 0x28, 0x4D, 0x50, 0x63, 0x66);
    }

    protected void registerItemRewriter(int... ids) {
        internalRewrite(rewriteOutbounds, ConnectionProtocol.PLAY, remapOutboundPacketId(ConnectionProtocol.PLAY, ids[0], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ServerboundContainerClickPacket(wrapper)));
        internalRewrite(rewriteOutbounds, ConnectionProtocol.PLAY, remapOutboundPacketId(ConnectionProtocol.PLAY, ids[1], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ServerboundSetCreativeModeSlotPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, ids[2], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundContainerSetContentPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, ids[3], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundContainerSetSlotPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, ids[4], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundMerchantOffersPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, ids[5], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundSetEntityDataPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, ids[6], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundSetEquipmentPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, ids[7], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundUpdateAdvancementsPacket(wrapper)));
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, ids[8], sourcePV), wrapper -> wrapper.readIsPassthrough(() -> new ClientboundUpdateRecipesPacket(wrapper)));
    }

    protected void registerParticleRewriter() {
        registerParticleRewriter(0x24);
    }

    protected void registerParticleRewriter(int packetId) {
        // ClientboundLevelParticlesPacket
        internalRewrite(rewriteInbounds, ConnectionProtocol.PLAY, remapInboundPacketId(ConnectionProtocol.PLAY, packetId, sourcePV), wrapper -> {
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

    @NotNull
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
        remapInbounds.computeIfAbsent(protocol.ordinal(), (k) -> new Int2IntOpenHashMap()).put(oldId, newId);
    }

    protected final void remapOutbound(@NotNull ConnectionProtocol protocol, int oldId, int newId) {
        if (!registeringOutbound) throw new IllegalStateException("Not registering outbound");
        remapOutbounds.computeIfAbsent(protocol.ordinal(), (k) -> new Int2IntOpenHashMap()).put(oldId, newId);
    }

    /**
     * @param protocol the protocol
     * @param newId packet id of newer version
     * @return packet id of older version
     */
    public final int getInboundId(@NotNull ConnectionProtocol protocol, int newId) {
        if (!remapInbounds.containsKey(protocol.ordinal())) return newId;
        return remapInbounds.get(protocol.ordinal()).getOrDefault(newId, newId);
    }

    /**
     * @param protocol the protocol
     * @param newId packet id of newer version
     * @return packet id of older version
     */
    public final int getOutboundId(@NotNull ConnectionProtocol protocol, int newId) {
        if (!remapOutbounds.containsKey(protocol.ordinal())) return newId;
        return remapOutbounds.get(protocol.ordinal()).getOrDefault(newId, newId);
    }

    protected final void rewriteInbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull Consumer<PacketWrapper> handler) {
        if (!registeringInbound) throw new IllegalStateException("Not registering inbound");
        int newId = remapInboundPacketId(protocol, oldId, sourcePV);
        internalRewrite(rewriteInbounds, protocol, newId, handler);
    }

    protected final void rewriteOutbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull Consumer<PacketWrapper> handler) {
        if (!registeringOutbound) throw new IllegalStateException("Not registering outbound");
        int newId = remapOutboundPacketId(protocol, oldId, sourcePV);
        internalRewrite(rewriteOutbounds, protocol, newId, handler);
    }

    protected final void internalRewrite(@NotNull Int2ObjectMap<Int2ObjectMap<ObjectArrayList<Consumer<PacketWrapper>>>> map, @NotNull ConnectionProtocol protocol, int oldId, @NotNull Consumer<PacketWrapper> handler) {
        map.computeIfAbsent(protocol.ordinal(), (k) -> new Int2ObjectOpenHashMap<>(4))
                .computeIfAbsent(oldId, (k) -> new ObjectArrayList<>())
                .add(handler);
    }

    public final void doRewriteInbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper) {
        PacketWrapperRewriter packetWrapperRewriter = new PacketWrapperRewriter(wrapper, this::rewriteInboundItemData);
        int newId = remapInboundPacketId(protocol, oldId, sourcePV, getFinalPV());
        doRewrite(protocol, newId, packetWrapperRewriter, rewriteInbounds);
    }

    public final void doRewriteOutbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper) {
        PacketWrapperRewriter packetWrapperRewriter = new PacketWrapperRewriter(wrapper, this::rewriteOutboundItemData);
        int newId = remapOutboundPacketId(protocol, oldId, sourcePV, getFinalPV());
        doRewrite(protocol, newId, packetWrapperRewriter, rewriteOutbounds);
    }

    private void doRewrite(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper, Int2ObjectMap<Int2ObjectMap<ObjectArrayList<Consumer<PacketWrapper>>>> map) {
        if (!map.containsKey(protocol.ordinal())) {
            wrapper.passthroughAll();
            return;
        }
        Collection<Consumer<PacketWrapper>> consumers = map.get(protocol.ordinal()).get(oldId);
        if (consumers == null || consumers.isEmpty()) {
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
            Field f = TagCollection.NetworkPayload.class.getDeclaredField("tags");
            f.setAccessible(true);
            return (Map<ResourceLocation, IntList>) f.get(networkPayload);
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
