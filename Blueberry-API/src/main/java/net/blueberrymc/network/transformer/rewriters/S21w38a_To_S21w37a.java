package net.blueberrymc.network.transformer.rewriters;

import net.blueberrymc.network.transformer.TransformableProtocolVersions;
import org.jetbrains.annotations.NotNull;

public class S21w38a_To_S21w37a extends S21w40a_To_S21w39a {
    public S21w38a_To_S21w37a() {
        this(TransformableProtocolVersions.SNAPSHOT_21W38A, TransformableProtocolVersions.SNAPSHOT_21W37A);
    }

    protected S21w38a_To_S21w37a(@NotNull TransformableProtocolVersions sourcePV, @NotNull TransformableProtocolVersions targetPV) {
        super(sourcePV, targetPV);
    }

    @Override
    protected void preRegister() {
        registerSoundRewriter();
    }

    @Override
    public void registerOutbound() {
    }

    @Override
    public void registerInbound() {
    }

    @Override
    protected int remapSoundId(int soundId) {
        // +---------------------------+---------------------------+
        // | 21w37a                    | 21w38a                    |
        // +---------------------------+---------------------------+
        // | 460 GRAVEL_STEP           | 460 GRAVEL_STEP           |
        // | 461 GRINDSTONE_USE        | 461 GRINDSTONE_USE        |
        // |                           | 462 GROWING_PLANT_CROP    |
        // | 462 GUARDIAN_AMBIENT      | 463 GUARDIAN_AMBIENT      |
        // | 463 GUARDIAN_AMBIENT_LAND | 464 GUARDIAN_AMBIENT_LAND |
        // | 464 GUARDIAN_ATTACK       | 465 GUARDIAN_ATTACK       |
        // +---------------------------+---------------------------+
        if (soundId >= 462) return soundId + 1;
        return soundId;
    }
}
