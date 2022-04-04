package net.blueberrymc.common.bml.event;

/**
 * CancellableEvent (API)
 * <p>This event can be overridden, so you don't have to implement isCancelled/setCancelled method.
 */
public abstract class CancellableEvent extends Event implements Cancellable {
    protected boolean cancelled = false;

    protected CancellableEvent() {
        super();
    }

    protected CancellableEvent(boolean async) {
        super(async);
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
