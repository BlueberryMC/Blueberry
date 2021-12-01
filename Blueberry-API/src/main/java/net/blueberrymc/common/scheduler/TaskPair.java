package net.blueberrymc.common.scheduler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A class used to hold the pair of client/server tasks.
 */
public record TaskPair(@Nullable BlueberryTask clientTask, @Nullable BlueberryTask serverTask) {
    @NotNull
    public BlueberryTask getTask() {
        if (clientTask == null && serverTask == null) {
            throw new IllegalArgumentException("Both clientTask and serverTask is null");
        }
        if (clientTask != null && serverTask != null) {
            throw new IllegalArgumentException("Both clientTask and serverTask is present");
        }
        return Objects.requireNonNullElse(clientTask, serverTask);
    }

    @Deprecated
    @Nullable
    public BlueberryTask getClientTask() {
        return clientTask;
    }

    @Deprecated
    @Nullable
    public BlueberryTask getServerTask() {
        return serverTask;
    }
}
