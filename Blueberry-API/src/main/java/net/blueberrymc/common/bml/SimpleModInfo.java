package net.blueberrymc.common.bml;

import net.blueberrymc.common.DeprecatedReason;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SimpleModInfo(@NotNull String name, @NotNull String modId) implements ModInfo {
    /**
     * @deprecated Use {@link #name()} instead.
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #name() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
    @NotNull
    @Override
    public String getName() {
        return name;
    }

    /**
     * @deprecated Use {@link #modId()} instead.
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #modId() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
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
