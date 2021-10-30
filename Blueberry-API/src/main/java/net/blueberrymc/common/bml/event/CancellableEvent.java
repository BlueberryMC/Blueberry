package net.blueberrymc.common.bml.event;

/**
 * CancellableEvent (API)
 * <p>This event can be overridden to avoid implementing isCancelled/setCancelled method on your class.
 */
public abstract class CancellableEvent extends Event implements Cancellable {
    protected boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
