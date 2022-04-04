package net.blueberrymc.common.permission;

import com.mojang.authlib.GameProfile;
import net.blueberrymc.common.Blueberry;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class DefaultPermissionProvider implements PermissionProvider {
    public static final DefaultPermissionProvider INSTANCE = new DefaultPermissionProvider();

    @Override
    public boolean hasPermissionForConsole(@NotNull String permission) {
        return true;
    }

    @Override
    public @NotNull PermissionState getPermissionStateForConsole(@NotNull String permission) {
        return PermissionState.TRUE;
    }

    @Override
    public @NotNull PermissionState getPermissionStateForPlayer(@NotNull UUID uuid, @NotNull String permission) {
        MinecraftServer server = Blueberry.getUtil().getMinecraftServer();
        if (server == null) return PermissionState.UNDEFINED;
        Optional<GameProfile> profile = server.getProfileCache().get(uuid);
        if (profile.isEmpty()) {
            return PermissionState.UNDEFINED;
        }
        if (server.getPlayerList().isOp(profile.get())) {
            return PermissionState.TRUE;
        } else {
            return PermissionState.FALSE;
        }
    }
}
