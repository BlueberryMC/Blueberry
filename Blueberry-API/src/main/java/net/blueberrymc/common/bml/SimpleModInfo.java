package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleModInfo that = (SimpleModInfo) o;
        return Objects.equals(name, that.name) && Objects.equals(modId, that.modId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, modId);
    }
}
