package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import org.jetbrains.annotations.NotNull;

public class S21w39a_To_S21w38a extends S21w40a_To_S21w39a {
    public S21w39a_To_S21w38a() {
        this(TransformableProtocolVersions.SNAPSHOT_21W39A, TransformableProtocolVersions.SNAPSHOT_21W38A);
    }

    protected S21w39a_To_S21w38a(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
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
