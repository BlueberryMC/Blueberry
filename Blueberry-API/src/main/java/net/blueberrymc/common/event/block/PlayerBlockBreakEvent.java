package net.blueberrymc.common.event.block;

import net.blueberrymc.common.bml.event.Cancellable;
import net.blueberrymc.common.bml.event.HandlerList;
import net.minecraft.server.level.ServerPlayer;
import net.blueberrymc.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the block is about to break. Please use {@link PlayerBlockDropItemEvent} for modifying item list. If a
 * player is trying to break with sword (or any item that cannot be used to destroy a block), the event will be fired
 * with cancelled state.
 */
public class PlayerBlockBreakEvent extends BlockExpEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    protected final ServerPlayer player;
    protected boolean cancelled = false;
    protected boolean dropItems = true;

    public PlayerBlockBreakEvent(@NotNull Block block, @NotNull ServerPlayer player) {
        super(block, 0);
        this.player = player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    public ServerPlayer getPlayer() {
        return player;
    }

    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    public boolean isDropItems() {
        return dropItems;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
