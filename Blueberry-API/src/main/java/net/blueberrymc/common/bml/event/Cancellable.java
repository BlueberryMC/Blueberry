package net.blueberrymc.common.bml.event;

public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancel);
}
