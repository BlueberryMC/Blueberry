package net.blueberrymc.common.permission;

import org.jetbrains.annotations.NotNull;

public interface PermissionHolder {
    /**
     * Checks if the holder has the provided permission.
     * @param permission the permission
     * @return true if holder has permission, false otherwise
     */
    default boolean hasPermission(@NotNull String permission) {
        return getPermissionState(permission).getValue();
    }

    /**
     * Checks if the holder has the provided permission.
     * @param permission the permission
     * @return TRUE if holder has permission, FALSE otherwise. UNDEFINED if result is not defined for provided permission.
     */
    @NotNull
    default PermissionState getPermissionState(@NotNull String permission) {
        return PermissionState.UNDEFINED;
    }
}
