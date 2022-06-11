package net.blueberrymc.common.bml;

import org.jetbrains.annotations.NotNull;

public record SimpleVersionedModInfo(@NotNull String name, @NotNull String modId, @NotNull String version) implements VersionedModInfo {
    @NotNull
    @Override
    public String getName() {
        return name;
    }

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

    /*
    @NotNull
    public static Optional<SimpleVersionedModInfo> load(@NotNull Dynamic<Tag> dynamic) {
        String name = dynamic.get("name").asString("");
        if (name.isEmpty()) return Optional.empty();
        String modId = dynamic.get("id").asString("");
        if (modId.isEmpty()) return Optional.empty();
        String version = dynamic.get("version").asString("");
        if (version.isEmpty()) return Optional.empty();
        return Optional.of(new SimpleVersionedModInfo(name, modId, version));
    }
    */
}
