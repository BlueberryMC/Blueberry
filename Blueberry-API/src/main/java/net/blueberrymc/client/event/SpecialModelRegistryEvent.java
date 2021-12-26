package net.blueberrymc.client.event;

import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * SpecialModelRegistryEvent can be used to call ModelBakery#addSpecialModel(ResourceLocation).
 * This event is always called asynchronously.
 */
public class SpecialModelRegistryEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public SpecialModelRegistryEvent() {
        super(true);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
