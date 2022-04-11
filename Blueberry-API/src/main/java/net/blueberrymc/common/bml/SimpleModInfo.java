package net.blueberrymc.common.bml;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SimpleModInfo(String name, String modId) implements ModInfo {
    /**
     * @deprecated Use {@link #name()} instead.
     */
    @Deprecated
    @NotNull
    @Override
    public String getName() {
        return name;
    }

    /**
     * @deprecated Use {@link #modId()} instead.
     */
    @Deprecated
    @NotNull
    @Override
    public String getModId() {
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
