package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.PacketRewriter;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagCollection;

public class S21w43a_To_S21w42a extends PacketRewriter {
    public S21w43a_To_S21w42a() {
        super(TransformableProtocolVersions.SNAPSHOT_21W43A, TransformableProtocolVersions.SNAPSHOT_21W42A);
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
                    addEmptyTags(value, "big_dripleaf_placeable");
                }
                value.write(wrapper.getWrite());
            });
        });
    }
}
