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

    /**
     * Returns the client name ("blueberry" if unmodified).
     * @return client name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns the Blueberry API version.
     * @return api version
     */
    @NotNull
    public String getVersion() {
        return version;
    }

    /**
     * Returns the commit hash of MagmaCube.
     * @return commit hash
     */
    @NotNull
    public String getMagmaCubeCommit() {
        return magmaCubeCommit;
    }

    /**
     * Returns the commit hash of Blueberry.
     * @return commit hash
     */
    @NotNull
    public String getCommit() {
        return commit;
    }

    /**
     * Returns the build date.
     * Example output: 2021-10-30T00:30:44+09:00 (yyyy-mm-ddThh:mm:ss+offset)
     * @return build date
     */
    @NotNull
    public String getBuiltAt() {
        return builtAt;
    }

    private String gameVersion = null;

    /**
     * Returns the game version. (e.g. 1.18, or 21w43a)
     * @return game version
     */
    @NotNull
    public String getGameVersion() {
        if (gameVersion != null) return gameVersion;
        return gameVersion = DetectedVersion.tryDetectVersion().getId();
    }

    private String fqv = null;

    /**
     * Returns the version string of [game version]-[name]-[api version].
     * @return [game version]-[name]-[api version]
     */
    @NotNull
    public String getFullyQualifiedVersion() {
        if (fqv != null) return fqv;
        return fqv = getGameVersion() + "-" + this.name + "-" + this.version;
    }
}
