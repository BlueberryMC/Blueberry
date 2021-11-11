package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.PacketWrapper;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class S21w42a_To_S21w41a extends S21w43a_To_S21w42a {
    public S21w42a_To_S21w41a() {
        this(TransformableProtocolVersions.SNAPSHOT_21W42A, TransformableProtocolVersions.SNAPSHOT_21W41A);
    }

    protected S21w42a_To_S21w41a(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    public void register() {
        registerSoundRewriter();
        registerItemRewriter();
        registerParticleRewriter();
        super.register();
    }

    @Override
    public void registerOutbound() {
    }

    @Override
    public void registerInbound() {
    }

    @NotNull
    @Override
    protected ItemStack rewriteOutboundItemData(@NotNull PacketWrapper wrapper) {
        if (wrapper.passthroughBoolean()) { // present
            int itemId = wrapper.getRead().readVarInt();
            if (itemId == 1027) itemId = 1; // MUSIC_DISC_OTHERSIDE doesn't exist in the server (replace with stone instead of air to avoid dummy item)
            if (itemId > 1027) itemId--;
            wrapper.writeVarInt(itemId);
            var count = wrapper.passthroughByte();
            var tag = wrapper.passthroughNbt();
            ItemStack item = new ItemStack(Item.byId(itemId), count);
            item.setTag(tag);
            return item;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @NotNull
    @Override
    protected ItemStack rewriteInboundItemData(@NotNull PacketWrapper wrapper) {
        if (wrapper.passthroughBoolean()) { // present
            int itemId = wrapper.getRead().readVarInt();
            if (itemId >= 1027) itemId++;
            wrapper.writeVarInt(itemId);
            var count = wrapper.passthroughByte();
            var tag = wrapper.passthroughNbt();
            ItemStack item = new ItemStack(Item.byId(itemId), count);
            item.setTag(tag);
            return item;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    protected int remapSoundId(int soundId) {
        // MUSIC_DRAGON, MUSIC_END, MUSIC_GAME, MUSIC_MENU, MUSIC_BIOME_BASALT_DELTAS
        if (soundId >= 633 && soundId <= 637) return soundId + 1;
        if (soundId == 638) return 645; // MUSIC_BIOME_NETHER_WASTES
        if (soundId == 639) return 648; // MUSIC_BIOME_SOUL_SAND_VALLEY
        if (soundId == 640) return 639; // MUSIC_BIOME_CRIMSON_FOREST
        if (soundId >= 641) return soundId + 9; // MUSIC_BIOME_WARPED_FOREST and later
        return soundId;
    }

    @Override
    protected int remapParticleId(int particleId) {
        // BARRIER (2) and LIGHT (3) moved to BLOCK_MARKER (3)
        //           21w41a           |           21w42a
        // -------------------------- | --------------------------
        //   0 AMBIENT_ENTITY_EFFECT  |   0 AMBIENT_ENTITY_EFFECT
        //   1 ANGRY_VILLAGER         |   1 ANGRY_VILLAGER
        // - 2 BARRIER -------┐       |
        // - 3 LIGHT ---------|       |
        //   4 BLOCK          |       |   2 BLOCK
        //                    └-----> | + 3 BLOCK_MARKER
        //   5 BUBBLE                 |   4 BUBBLE
        //
        if (particleId == 2) {
            return 3;
        }
        if (particleId == 4) {
            return 2;
        }
        if (particleId > 4) {
            return particleId - 1;
        }
        return particleId;
    }
}
