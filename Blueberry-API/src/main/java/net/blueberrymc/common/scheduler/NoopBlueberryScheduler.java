package net.blueberrymc.common.scheduler;

/**
 * A task scheduler that does nothing.
 */
public class NoopBlueberryScheduler extends AbstractBlueberryScheduler {
    public static final NoopBlueberryScheduler INSTANCE = new NoopBlueberryScheduler();

    @Override
    public void tick() {
    }

    @Override
    public void tickAsync() {
    }
}
