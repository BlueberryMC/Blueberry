package net.blueberrymc.common.scheduler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A record that holds the pair of client/server tasks.
 */
public record TaskPair(@Nullable BlueberryTask clientTask, @Nullable BlueberryTask serverTask) {
    /**
     * Returns a task. If both tasks are null, the method will throw exception.
     * @return a task
     * @throws IllegalStateException if both tasks are present or both tasks are null
     */
    @NotNull
    public BlueberryTask getTask() {
        if (clientTask == null && serverTask == null) {
            throw new IllegalArgumentException("Both clientTask and serverTask is null");
        }
        return Objects.requireNonNullElse(clientTask, serverTask);
    }
}
