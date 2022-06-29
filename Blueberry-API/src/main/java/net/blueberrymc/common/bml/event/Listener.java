package net.blueberrymc.common.bml.event;

import net.blueberrymc.common.DeprecatedReason;
import org.jetbrains.annotations.ApiStatus;

/**
 * This indicates that the class is listening to events. Required to register events.
 * @deprecated Listener interface shouldn't have existed.
 */
@ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
@DeprecatedReason("Listener interface shouldn't have existed.")
@Deprecated(forRemoval = true)
public interface Listener {
}
