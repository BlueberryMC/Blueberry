package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public class SimpleModInfo implements ModInfo {
    private final String name;
    private final String modId;

    public SimpleModInfo(@NotNull String name, @NotNull String modId) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(modId, "modId cannot be null");
        this.name = name;
        this.modId = modId;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getModId() {
        return modId;
    }
}
