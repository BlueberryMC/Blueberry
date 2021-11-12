package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import org.jetbrains.annotations.NotNull;

public class S21w41a_To_S21w40a extends S21w42a_To_S21w41a {
    public S21w41a_To_S21w40a() {
        this(TransformableProtocolVersions.SNAPSHOT_21W41A, TransformableProtocolVersions.SNAPSHOT_21W40A);
    }

    protected S21w41a_To_S21w40a(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    protected void preRegister() {
    }

    @Override
    public void registerOutbound() {
    }

    @Override
    public void registerInbound() {
    }
}
