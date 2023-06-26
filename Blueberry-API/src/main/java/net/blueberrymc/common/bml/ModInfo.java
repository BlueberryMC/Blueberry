package net.blueberrymc.common.bml;

import net.blueberrymc.common.DeprecatedReason;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface ModInfo {
    /**
     * @deprecated Use {@link #name()} instead.
     */
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #name() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
    @NotNull
    default String getName() {
        return name();
    }

    /**
     * @deprecated Use {@link #modId()} instead.
     */
    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #name() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
    @NotNull
    default String getModId() {
        return modId();
    }

    @NotNull
    String name();

    @NotNull
    String modId();
}
