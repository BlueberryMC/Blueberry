package net.blueberrymc.client.scheduler;

import net.blueberrymc.client.event.gameevent.ClientTickEvent;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.scheduler.AbstractBlueberryScheduler;

/**
 * Client task scheduler
 */
public class BlueberryClientScheduler extends AbstractBlueberryScheduler {
    @Override
    public void tick() {
        super.tick();
        Blueberry.getEventManager().callEvent(ClientTickEvent.INSTANCE);
    }
}
