package net.blueberrymc.common.util;

import net.minecraft.DetectedVersion;
import org.jetbrains.annotations.NotNull;

public class BlueberryVersion {
    @NotNull private final String name;
    @NotNull private final String version;
    @NotNull private final String magmaCubeCommit;
    @NotNull private final String commit;
    @NotNull private final String builtAt;

    public BlueberryVersion(@NotNull String name, @NotNull String version, @NotNull String magmaCubeCommit, @NotNull String commit, @NotNull String builtAt) {
        this.name = name;
        this.version = version;
        this.magmaCubeCommit = magmaCubeCommit;
        this.commit = commit;
        this.builtAt = builtAt;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    @NotNull
    public String getMagmaCubeCommit() {
        return magmaCubeCommit;
    }

    @NotNull
    public String getCommit() {
        return commit;
    }

    @NotNull
    public String getBuiltAt() {
        return builtAt;
    }

    private String gameVersion = null;

    @NotNull
    public String getGameVersion() {
        if (gameVersion != null) return gameVersion;
        return gameVersion = DetectedVersion.tryDetectVersion().getId();
    }

    private String fqv = null;

    @NotNull
    public String getFullyQualifiedVersion() {
        if (fqv != null) return fqv;
        return fqv = getGameVersion() + "-" + this.name + "-" + this.version;
    }
}
