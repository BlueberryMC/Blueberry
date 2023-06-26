package net.blueberrymc.common.bml;

import com.mojang.serialization.Dynamic;
import net.blueberrymc.common.DeprecatedReason;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SimpleVersionedModInfo(@NotNull String name, @NotNull String modId, @NotNull String version) implements VersionedModInfo {
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #name() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #modId() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
    @NotNull
    @Override
    public String getModId() {
        return modId;
    }

    @NotNull
    @Override
    public String getVersion() {
        return version;
    }

    @NotNull
    public static Optional<SimpleVersionedModInfo> load(@NotNull Dynamic<?> dynamic) {
        String name = dynamic.get("name").asString("");
        if (name.isEmpty()) return Optional.empty();
        String modId = dynamic.get("id").asString("");
        if (modId.isEmpty()) return Optional.empty();
        String version = dynamic.get("version").asString("");
        if (version.isEmpty()) return Optional.empty();
        return Optional.of(new SimpleVersionedModInfo(name, modId, version));
    }
}
