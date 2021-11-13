package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.TransformableProtocolVersions;
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
    }

    @Override
    public void registerOutbound() {
    }
}
