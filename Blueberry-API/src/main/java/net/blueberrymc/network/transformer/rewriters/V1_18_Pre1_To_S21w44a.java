package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.PacketRewriter;
import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import org.jetbrains.annotations.NotNull;

public class V1_18_Pre1_To_S21w44a extends PacketRewriter {
    public V1_18_Pre1_To_S21w44a() {
        this(TransformableProtocolVersions.v1_18_PRE1, TransformableProtocolVersions.SNAPSHOT_21W44A);
    }

    protected V1_18_Pre1_To_S21w44a(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    public void registerOutbound() {
    }

    @Override
    public void registerInbound() {
    }
}
