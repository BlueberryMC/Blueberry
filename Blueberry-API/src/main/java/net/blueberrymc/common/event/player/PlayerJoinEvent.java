package net.blueberrymc.common.event.player;

import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.server.entity.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Fired when a player joins the server. This event is called after the player has been added to the level.
 */
public class PlayerJoinEvent extends Event {
    private final ServerPlayer player;

    public PlayerJoinEvent(@NotNull ServerPlayer player) {
        this.player = Objects.requireNonNull(player, "player cannot be null");
    }

    /**
     * Gets the player that joined the server.
     * @return the player
     */
    @NotNull
    public ServerPlayer getPlayer() {
        return player;
    }
}
