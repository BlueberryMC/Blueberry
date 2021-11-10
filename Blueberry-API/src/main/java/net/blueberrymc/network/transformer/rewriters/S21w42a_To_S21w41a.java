package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.PacketRewriter;
import net.blueberrymc.network.transformer.PacketWrapper;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class S21w42a_To_S21w41a extends PacketRewriter {
    public S21w42a_To_S21w41a() {
        super(TransformableProtocolVersions.SNAPSHOT_21W42A, TransformableProtocolVersions.SNAPSHOT_21W41A);
    }

    @Override
    public void registerOutbound() {
        // Remap Item IDs
        rewriteOutbound(ConnectionProtocol.PLAY, 0x08, wrapper -> wrapper.readIsPassthrough(() -> new ServerboundContainerClickPacket(wrapper)));
        rewriteOutbound(ConnectionProtocol.PLAY, 0x28, wrapper -> wrapper.readIsPassthrough(() -> new ServerboundSetCreativeModeSlotPacket(wrapper)));
    }

    @Override
    public void registerInbound() {
        // Remap Item IDs
        rewriteInbound(ConnectionProtocol.PLAY, 0x14, wrapper -> wrapper.readIsPassthrough(() -> new ClientboundContainerSetContentPacket(wrapper)));
        rewriteInbound(ConnectionProtocol.PLAY, 0x16, wrapper -> wrapper.readIsPassthrough(() -> new ClientboundContainerSetSlotPacket(wrapper)));
        rewriteInbound(ConnectionProtocol.PLAY, 0x28, wrapper -> wrapper.readIsPassthrough(() -> new ClientboundMerchantOffersPacket(wrapper)));
        rewriteInbound(ConnectionProtocol.PLAY, 0x4D, wrapper -> wrapper.readIsPassthrough(() -> new ClientboundSetEntityDataPacket(wrapper)));
        rewriteInbound(ConnectionProtocol.PLAY, 0x50, wrapper -> wrapper.readIsPassthrough(() -> new ClientboundSetEquipmentPacket(wrapper)));
        rewriteInbound(ConnectionProtocol.PLAY, 0x62, wrapper -> wrapper.readIsPassthrough(() -> new ClientboundUpdateAdvancementsPacket(wrapper)));
        rewriteInbound(ConnectionProtocol.PLAY, 0x65, wrapper -> wrapper.readIsPassthrough(() -> new ClientboundUpdateRecipesPacket(wrapper)));
        // ClientboundLevelParticlesPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x24, wrapper -> {
            // BARRIER (2) and LIGHT (3) moved to BLOCK_MARKER (3)
            int particleId = wrapper.readInt(); // Particle ID
            if (particleId == 2) {
                particleId = 3;
            }
            if (particleId == 4) {
                particleId = 2;
            }
            if (particleId > 4) {
                particleId--;
            }
            wrapper.writeInt(particleId);
            wrapper.passthroughAll();
        });
        // ClientboundSoundPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x5C, wrapper -> {
            int soundId = wrapper.readVarInt();
            // MUSIC_DRAGON, MUSIC_END, MUSIC_GAME, MUSIC_MENU, MUSIC_BIOME_BASALT_DELTAS
            if (soundId >= 633 && soundId <= 637) {
                soundId++;
            }
            if (soundId == 638) soundId = 645; // MUSIC_BIOME_NETHER_WASTES
            if (soundId == 639) soundId = 648; // MUSIC_BIOME_SOUL_SAND_VALLEY
            if (soundId == 640) soundId = 639; // MUSIC_BIOME_CRIMSON_FOREST
            if (soundId >= 641) soundId += 9; // MUSIC_BIOME_WARPED_FOREST and later
            wrapper.writeVarInt(soundId);
            wrapper.passthroughAll();
        });
    }

    @NotNull
    @Override
    protected ItemStack rewriteItemData(@NotNull PacketWrapper wrapper) {
        if (wrapper.passthroughBoolean()) { // present
            int itemId = wrapper.readVarInt();
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
}
