package net.blueberrymc.common.scheduler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class used to hold the pair of client/server tasks.
 */
public class TaskPair {
    private final BlueberryTask clientTask;
    private final BlueberryTask serverTask;

    public TaskPair(@Nullable BlueberryTask clientTask, @Nullable BlueberryTask serverTask) {
        this.clientTask = clientTask;
        this.serverTask = serverTask;
    }

    @NotNull
    public BlueberryTask getTask() {
        if (clientTask == null && serverTask == null) {
            throw new IllegalArgumentException("Both clientTask and serverTask is null");
        }
        if (clientTask != null && serverTask != null) {
            throw new IllegalArgumentException("Both clientTask and serverTask is present (use getClientTask and getServerTask)");
        }
        if (clientTask == null) return serverTask;
        return clientTask;
    }

    @Nullable
    public BlueberryTask getClientTask() {
        return clientTask;
    }

    @Nullable
    public BlueberryTask getServerTask() {
        return serverTask;
    }
}
