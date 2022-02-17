package net.blueberrymc.client.event;

import net.blueberrymc.common.bml.event.Event;

/**
 * SpecialModelRegistryEvent can be used to call ModelBakery#addSpecialModel(ResourceLocation).
 * This event is always called asynchronously.
 */
public class SpecialModelRegistryEvent extends Event {
    public SpecialModelRegistryEvent() {
        super(true);
    }
}
