package net.blueberrymc.common.event.mod;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.loading.ModLoadingError;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Called when the {@link net.blueberrymc.common.bml.loading.ModLoadingError} object is being added to the
 * {@link net.blueberrymc.common.bml.loading.ModLoadingErrors} list.
 */
public class ModLoadingErrorAddEvent extends Event {
    private final ModLoadingError error;

    public ModLoadingErrorAddEvent(@NotNull ModLoadingError error) {
        super(!Blueberry.getUtil().isOnGameThread()); // this event may be called from any thread
        this.error = Objects.requireNonNull(error);
    }

    /**
     * Gets the {@link net.blueberrymc.common.bml.loading.ModLoadingError} object being added to the
     * {@link net.blueberrymc.common.bml.loading.ModLoadingErrors} list.
     * @return the error
     */
    @NotNull
    public ModLoadingError getError() {
        return error;
    }
}
