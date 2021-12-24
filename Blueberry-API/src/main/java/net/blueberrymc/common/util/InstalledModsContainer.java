package net.blueberrymc.common.util;

import net.blueberrymc.common.bml.VersionedModInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public interface InstalledModsContainer {
    void setInstalledMods(@NotNull Collection<? extends VersionedModInfo> collection);

    @NotNull
    Set<VersionedModInfo> getInstalledMods();
}
