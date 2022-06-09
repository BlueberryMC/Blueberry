package net.blueberrymc.common.scheduler;

import net.blueberrymc.common.DeprecatedReason;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A record that holds the pair of client/server tasks.
 */
public record TaskPair(@Nullable BlueberryTask clientTask, @Nullable BlueberryTask serverTask) {
    /**
     * Returns a task. If both tasks are present or both tasks are null, the method will throw exception.
     * @return a task
     * @throws IllegalStateException if both tasks are present or both tasks are null
     */
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

    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #clientTask() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Nullable
    public BlueberryTask getClientTask() {
        return clientTask;
    }

    @Deprecated(forRemoval = true)
    @DeprecatedReason("Use #serverTask() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Nullable
    public BlueberryTask getServerTask() {
        return serverTask;
    }
}
