package net.blueberrymc.common.event.block;

import net.blueberrymc.common.bml.event.Cancellable;
import net.blueberrymc.world.level.block.Block;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called if the player breaks the block then drops the items. If the event was cancelled, the items will not be
 * dropped. This event cannot cancel the block break. Please use {@link PlayerBlockBreakEvent} for that. Clearing the
 * list of items will also prevent from dropping items. Cancelling the event will prevent the items from dropping.
 */
public class PlayerBlockDropItemEvent extends BlockEvent implements Cancellable {
    private final ServerPlayer player;
    private final List<ItemEntity> items;
    private boolean cancelled = false;

    public PlayerBlockDropItemEvent(@NotNull Block block, @NotNull ServerPlayer player, @NotNull List<ItemEntity> items) {
        super(block);
        this.player = player;
        this.items = items;
    }

    /**
     * Returns a player who triggered this event.
     * @return the player
     */
    @NotNull
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * Gets items for this event. The returned list is mutable.
     * @return the items list
     */
    @NotNull
    public List<ItemEntity> getItems() {
        return items;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
