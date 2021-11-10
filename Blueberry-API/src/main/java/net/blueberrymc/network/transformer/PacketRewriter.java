package net.blueberrymc.network.transformer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.blueberrymc.native_util.NativeUtil;
import net.minecraft.network.ConnectionProtocol;
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

    /**
     * @param sourcePV source protocol version (pv before rewrite)
     * @param targetPV target protocol version (pv after rewrite)
     */
    public PacketRewriter(int sourcePV, int targetPV) {
        this.sourcePV = sourcePV;
        this.targetPV = targetPV;
        registeringInbound = true;
        preRegisterInbound();
        registerInbound();
        registeringInbound = false;
        registeringOutbound = true;
        preRegisterOutbound();
        registerOutbound();
        registeringOutbound = false;
    }

    public PacketRewriter(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
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

    private void preRegisterInbound() {
    }

    private void preRegisterOutbound() {
        // ClientIntentionPacket (server-bound)
        // this implementation just changes the protocol version
        rewriteOutbound(ConnectionProtocol.HANDSHAKING, 0x00, wrapper -> {
            wrapper.readVarInt();
            wrapper.writeVarInt(getTargetPV());
            wrapper.passthroughAll();
        });
    }

    @NotNull
    protected ItemStack rewriteItemData(@NotNull PacketWrapper wrapper) {
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
        rewriteInbounds.computeIfAbsent(protocol, (k) -> ArrayListMultimap.create(4, 3)).put(oldId, handler);
    }

    protected final void rewriteOutbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull Consumer<PacketWrapper> handler) {
        if (!registeringOutbound) throw new IllegalStateException("Not registering outbound");
        rewriteOutbounds.computeIfAbsent(protocol, (k) -> ArrayListMultimap.create(4, 3)).put(oldId, handler);
    }

    public final void doRewriteInbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper) {
        PacketWrapperRewriter packetWrapperRewriter = new PacketWrapperRewriter(wrapper);
        doRewrite(protocol, oldId, packetWrapperRewriter, rewriteInbounds);
    }

    public final void doRewriteOutbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper) {
        PacketWrapperRewriter packetWrapperRewriter = new PacketWrapperRewriter(wrapper);
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

    public class PacketWrapperRewriter extends PacketWrapper {
        public PacketWrapperRewriter(@NotNull PacketWrapper wrapper) {
            super(wrapper.getRead(), wrapper.getWrite());
        }

        @Override
        public @NotNull ItemStack passthroughItem() {
            return PacketRewriter.this.rewriteItemData(this);
        }

        @Override
        public @NotNull PacketWrapper passthrough(@NotNull Type type) {
            if (type == Type.ITEM) {
                PacketRewriter.this.rewriteItemData(this);
                return this;
            }
            return super.passthrough(type);
        }
    }
}
