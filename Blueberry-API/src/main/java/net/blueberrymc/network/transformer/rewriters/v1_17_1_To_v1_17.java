package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.PacketWrapper;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class v1_17_1_To_v1_17 extends S21w37a_To_v1_17_1 {
    public v1_17_1_To_v1_17() {
        this(TransformableProtocolVersions.v1_17_1, TransformableProtocolVersions.v1_17);
    }

    protected v1_17_1_To_v1_17(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    protected void preRegister() {
    }

    @Override
    public void registerInbound() {
        // ClientboundContainerSetContentPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x14, wrapper -> {
            wrapper.passthrough(PacketWrapper.Type.UNSIGNED_BYTE); // Container ID
            wrapper.writeVarInt(0); // State ID
            int length = wrapper.readShort(); // Count
            wrapper.writeVarInt(length); // Count
            for (int i = 0; i < length; i++) {
                wrapper.passthrough(PacketWrapper.Type.ITEM); // Item
            }
            wrapper.writeItem(ItemStack.EMPTY); // Carried Item
        });
        // ClientboundContainerSetSlotPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x16, wrapper -> {
            wrapper.passthrough(PacketWrapper.Type.BYTE); // Container ID
            wrapper.writeVarInt(0); // State ID
            wrapper.passthrough(PacketWrapper.Type.SHORT); // Slot
            wrapper.passthrough(PacketWrapper.Type.ITEM); // Item
        });
        // ClientboundRemoveEntityPacket -> ClientboundRemoveEntitiesPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x3A, wrapper -> {
            wrapper.writeVarInt(1); // Count
            wrapper.passthrough(PacketWrapper.Type.VAR_INT); // Entity IDs
        });
    }

    @Override
    public void registerOutbound() {
        rewriteOutbound(ConnectionProtocol.PLAY, 0x08, wrapper -> {
            wrapper.passthrough(PacketWrapper.Type.BYTE); // Container ID
            wrapper.readVarInt(); // State ID
            wrapper.passthroughAll(); // slotNum, buttonNum, clickType, carriedItem, changedSlots
        });
        rewriteOutbound(ConnectionProtocol.PLAY, 0x0B, wrapper -> {
            CompoundTag tag = new CompoundTag();
            int slot = wrapper.readVarInt(); // Slot
            int pages = wrapper.readVarInt(); // Pages count
            ListTag pagesTag = new ListTag();
            for (int i = 0; i < pages; i++) {
                String page = wrapper.readUtf(); // Page
                pagesTag.add(StringTag.valueOf(page));
            }
            tag.put("pages", pagesTag);
            boolean hasTitle = wrapper.readBoolean();
            if (hasTitle) { // Has title
                String title = wrapper.readUtf(128);
                tag.put("title", StringTag.valueOf(title));
            }
            wrapper.writeBoolean(true); // Item is not air
            wrapper.writeVarInt(942); // Writable book ID
            wrapper.writeByte(1); // Item count
            wrapper.writeNbt(tag); // Book tag
            wrapper.writeBoolean(hasTitle); // Signing
            wrapper.writeVarInt(slot); // Slot
        });
    }
}
