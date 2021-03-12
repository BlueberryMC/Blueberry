package net.blueberrymc.server.scheduler;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;
import net.blueberrymc.server.event.gameevent.ServerTickEvent;

public class BlueberryServerScheduler extends AbstractBlueberryScheduler {
    @Override
    public void tick() {
        super.tick();
        Blueberry.getEventManager().callEvent(ServerTickEvent.INSTANCE);
    }

    @Override
    public void tickAsync() {
        super.tickAsync();
    }
}
