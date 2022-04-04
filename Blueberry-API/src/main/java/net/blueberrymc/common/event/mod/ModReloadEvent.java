package net.blueberrymc.common.event.mod;

import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.event.CancellableEvent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Fired when a user tries to reload a mod using ModListScreen or via command. Cancelling the event will prevent the
 * mod from being reloaded.
 */
public class ModReloadEvent extends CancellableEvent {
    private final ServerPlayer player;
    private final BlueberryMod mod;

    public ModReloadEvent(@Nullable ServerPlayer player, @NotNull BlueberryMod mod) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(mod, "mod cannot be null");
        this.player = player;
        this.mod = mod;
    }

    /**
     * Checks if player has initiated the mod reload.
     * @return true if player is defined, false otherwise.
     */
    public boolean isFromPlayer() {
        return player != null;
    }

    /**
     * Get the actor who initiated the mod reload.
     * @return the player, null if not a player (e.g. console) or done via ModListScreen
     */
    @Nullable
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * Get the mod being reloaded.
     * @return the mod
     */
    @NotNull
    public BlueberryMod getMod() {
        return mod;
    }
}
