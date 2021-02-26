package net.blueberrymc.client.event.render;

import net.blueberrymc.common.bml.event.Event;

public abstract class RenderEvent extends Event {
    public RenderEvent() {
        super();
    }

    public RenderEvent(boolean async) {
        super(async);
    }
}
