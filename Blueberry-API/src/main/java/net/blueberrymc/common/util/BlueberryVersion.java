package net.blueberrymc.common.util;

import net.blueberrymc.util.DetectedVersion;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BlueberryVersion {
    @NotNull private final String name;
    @NotNull private final String version;
    @NotNull private final String magmaCubeCommit;
    @NotNull private final String commit;
    @NotNull private final String builtAt;
    @NotNull private final String branch;
    private final int buildNumber;

    public BlueberryVersion(@NotNull String name,
                            @NotNull String version,
                            @NotNull String magmaCubeCommit,
                            @NotNull String commit,
                            @NotNull String builtAt,
                            @NotNull String branch,
                            int buildNumber) {
        this.name = name;
        this.version = version;
        this.magmaCubeCommit = magmaCubeCommit;
        this.commit = commit;
        this.builtAt = builtAt;
        this.branch = branch;
        this.buildNumber = buildNumber;
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
     * Returns the commit hash of MagmaCube but limited to 10 characters max.
     * @return commit hash (10 chars maximum)
     */
    @NotNull
    public String getShortMagmaCubeCommit() {
        return magmaCubeCommit.substring(0, Math.min(10, magmaCubeCommit.length()));
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
     * Returns the commit hash of Blueberry but limited to 10 characters max.
     * @return commit hash (10 chars maximum)
     */
    @NotNull
    public String getShortCommit() {
        return commit.substring(0, Math.min(10, commit.length()));
    }

    /**
     * Returns the build date.
     * Example output:
     * <ul>
     *     <li>2021-10-30T00:30:44+00:00 (yyyy-mm-ddThh:mm:ss+offset)</li>
     *     <li>2022-01-01T00:00:00Z (yyyy-mm-ddThh:mm:ssZ) (Z means UTC)</li>
     * </ul>
     * @return build date
     */
    @NotNull
    public String getBuiltAt() {
        return builtAt;
    }

    /**
     * Returns the branch where the jar was built at.
     * @return branch
     */
    @NotNull
    public String getBranch() {
        return branch;
    }

    /**
     * Returns the build number for the build. Returns 0 if unknown or build number is 0.
     * @return build number
     */
    public int getBuildNumber() {
        return buildNumber;
    }

    private String gameVersion = null;

    /**
     * Returns the game version. (e.g. 1.18-pre1, 1.18, or 21w43a)
     * @return game version
     */
    @NotNull
    public String getGameVersion() {
        if (gameVersion != null) return gameVersion;
        return gameVersion = Objects.requireNonNull(DetectedVersion.tryDetectVersion()).getId();
    }

    private String fqv = null;

    /**
     * Returns the version string of [game version]-[api version].[build-number].
     * @return [game version]-[name]-[api version]
     */
    @NotNull
    public String getFullyQualifiedVersion() {
        if (fqv != null) return fqv;
        return fqv = getGameVersion() + "-" + this.version + "." + buildNumber;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlueberryVersion that = (BlueberryVersion) o;
        return name.equals(that.name) &&
                version.equals(that.version) &&
                magmaCubeCommit.equals(that.magmaCubeCommit) &&
                commit.equals(that.commit) &&
                builtAt.equals(that.builtAt) &&
                Objects.equals(gameVersion, that.gameVersion) &&
                Objects.equals(branch, that.branch) &&
                buildNumber == that.buildNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, magmaCubeCommit, commit, builtAt, gameVersion, branch, buildNumber);
    }
}
