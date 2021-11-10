package net.blueberrymc.network.transformer;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.blueberrymc.native_util.NativeUtil;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagCollection;
import org.jetbrains.annotations.NotNull;

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
    private final Map<ConnectionProtocol, Map<Integer, Consumer<PacketWrapper>>> rewriteInbounds = new HashMap<>();
    private final Map<ConnectionProtocol, Map<Integer, Consumer<PacketWrapper>>> rewriteOutbounds = new HashMap<>();
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
        registerInbound();
        registeringInbound = false;
        registeringOutbound = true;
        registerOutbound();
        registeringOutbound = false;
    }

    public int getSourcePV() {
        return sourcePV;
    }

    public int getTargetPV() {
        return targetPV;
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
        rewriteInbounds.computeIfAbsent(protocol, (k) -> new HashMap<>()).put(oldId, handler);
    }

    protected final void rewriteOutbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull Consumer<PacketWrapper> handler) {
        if (!registeringOutbound) throw new IllegalStateException("Not registering outbound");
        rewriteOutbounds.computeIfAbsent(protocol, (k) -> new HashMap<>()).put(oldId, handler);
    }

    public final void doRewriteInbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper) {
        doRewrite(protocol, oldId, wrapper, rewriteInbounds);
    }

    public final void doRewriteOutbound(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper) {
        doRewrite(protocol, oldId, wrapper, rewriteOutbounds);
    }

    private void doRewrite(@NotNull ConnectionProtocol protocol, int oldId, @NotNull PacketWrapper wrapper, Map<ConnectionProtocol, Map<Integer, Consumer<PacketWrapper>>> rewriteOutbounds) {
        if (!rewriteOutbounds.containsKey(protocol)) {
            wrapper.passthroughAll();
            return;
        }
        Consumer<PacketWrapper> consumer = rewriteOutbounds.get(protocol).get(oldId);
        if (consumer == null) {
            wrapper.passthroughAll();
            return;
        }
        consumer.accept(wrapper);
    }

    // helper methods

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
}
