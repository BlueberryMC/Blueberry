package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.PacketRewriter;
import net.blueberrymc.network.transformer.PacketWrapper;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagCollection;
import org.jetbrains.annotations.NotNull;

public class S21w44a_To_S21w43a extends v1_18_Pre5_To_v1_18_Pre4 {
    public S21w44a_To_S21w43a() {
        this(TransformableProtocolVersions.SNAPSHOT_21W44A, TransformableProtocolVersions.SNAPSHOT_21W43A);
    }

    protected S21w44a_To_S21w43a(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    public void registerOutbound() {
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
