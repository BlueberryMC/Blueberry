package net.blueberrymc.common.permission;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PermissionProvider {
    /**
     * Checks if the console has the provided permission.
     * @param permission the permission
     * @return true if holder has permission, false otherwise
     */
    default boolean hasPermissionForConsole(@NotNull String permission) {
        return getPermissionStateForConsole(permission).getValue();
    }

    /**
     * Checks if the console has the provided permission.
     * @param permission the permission
     * @return TRUE if holder has permission, FALSE otherwise. UNDEFINED if result is not defined for provided permission.
     */
    @NotNull
    default PermissionState getPermissionStateForConsole(@NotNull String permission) {
        return PermissionState.UNDEFINED;
    }

    /**
     * Checks if the player has the provided permission.
     * @param uuid uuid of the player
     * @param permission the permission
     * @return true if holder has permission, false otherwise
     */
    default boolean hasPermissionForPlayer(@NotNull UUID uuid, @NotNull String permission) {
        return getPermissionStateForPlayer(uuid, permission).getValue();
    }

    /**
     * Checks if the player has the provided permission.
     * @param uuid uuid of the player
     * @param permission the permission
     * @return TRUE if holder has permission, FALSE otherwise. UNDEFINED if result is not defined for provided permission.
     */
    @NotNull
    default PermissionState getPermissionStateForPlayer(@NotNull UUID uuid, @NotNull String permission) {
        return PermissionState.UNDEFINED;
    }
}
