package net.blueberrymc.common.event.lifecycle;

import net.blueberrymc.common.bml.event.Event;

public class RegistryBootstrappedEvent extends Event {
    private static final RegistryBootstrappedEvent INSTANCE = new RegistryBootstrappedEvent();

    private RegistryBootstrappedEvent() {
    }

    public static void fire() {
        callEvent(INSTANCE);
    }
}
