package net.blueberrymc.network.mod;

import org.jetbrains.annotations.NotNull;

public class ModInfo {
    public final String modId;
    public final String version;

    public ModInfo(@NotNull String modId, @NotNull String version) {
        this.modId = modId;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModInfo modInfo = (ModInfo) o;

        if (!modId.equals(modInfo.modId)) return false;
        return version.equals(modInfo.version);
    }

    @Override
    public int hashCode() {
        int result = modId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ModInfo{" + "modId='" + modId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
