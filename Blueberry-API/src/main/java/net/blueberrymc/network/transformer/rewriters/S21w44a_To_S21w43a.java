package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.PacketRewriter;
import net.blueberrymc.network.transformer.PacketWrapper;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagCollection;

public class S21w44a_To_S21w43a extends PacketRewriter {
    public S21w44a_To_S21w43a() {
        super(0x40000030, 0x4000002F);
    }

    @Override
    public void registerOutbound() {
        // ClientIntentionPacket (server-bound)
        rewriteOutbound(ConnectionProtocol.HANDSHAKING, 0x00, wrapper -> {
            wrapper.readVarInt();
            wrapper.writeVarInt(getTargetPV());
            wrapper.passthroughAll();
        });
        // ServerboundClientInformationPacket
        rewriteOutbound(ConnectionProtocol.PLAY, 0x05, wrapper -> {
            wrapper.passthroughUtf(16); // language
            wrapper.passthrough(PacketWrapper.Type.BYTE); // view distance
            wrapper.passthrough(PacketWrapper.Type.ENUM); // chat visibility
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // chat colors
            wrapper.passthrough(PacketWrapper.Type.UNSIGNED_BYTE); // model customization
            wrapper.passthrough(PacketWrapper.Type.ENUM); // main hand
            wrapper.passthrough(PacketWrapper.Type.BOOLEAN); // text filtering
            // allows listing (boolean)
        });
    }

    @Override
    public void registerInbound() {
        // ClientboundUpdateTagsPacket
        rewriteInbound(ConnectionProtocol.PLAY, 0x67, wrapper -> {
            var tags = wrapper.readMap((buf) -> ResourceKey.createRegistryKey(buf.readResourceLocation()), TagCollection.NetworkPayload::read);
            wrapper.writeVarInt(tags.size());
            tags.forEach((key, value) -> {
                wrapper.writeResourceLocation(key.location());
                if (key.location().getNamespace().equals("minecraft") && key.location().getPath().equals("block")) {
                    String[] newTags = {
                            "animals_spawnable_on",
                            "axolotls_spawnable_on",
                            "foxes_spawnable_on",
                            "goats_spawnable_on",
                            "mooshrooms_spawnable_on",
                            "parrots_spawnable_on",
                            "polar_bears_spawnable_on_in_frozen_ocean",
                            "rabbits_spawnable_on",
                            "wolves_spawnable_on",
                    };
                    addEmptyTags(value, newTags);
                }
                value.write(wrapper.getWrite());
            });
        });
    }
}
