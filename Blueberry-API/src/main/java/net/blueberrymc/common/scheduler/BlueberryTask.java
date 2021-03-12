package net.blueberrymc.common.scheduler;

import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.NotNull;

public interface BlueberryTask {
    @NotNull
    AbstractBlueberryScheduler getScheduler();

    /**
     * Get the task associated with this runnable.
     * @return the task associated with this runnable.
     */
    @NotNull
    default BlueberryTask getTask() { return this; }

    /**
     * Cancel the execution of this task.
     */
    void cancel();

    /**
     * @return whether the task was canceled.
     */
    boolean isCancelled();

    /**
     * Returns the task ID.
     * @return the task ID
     */
    long getTaskId();

    /**
     * @return true if this task is synchronous, false otherwise.
     */
    boolean isSync();

    /**
     * @return true if this task is asynchronous, false otherwise.
     */
    default boolean isAsync() { return !isSync(); }

    /**
     * Get delay time for this task.
     * @return the time or -1 if this isn't delayed task
     */
    long getDelayTime();

    /**
     * Get interval/repeat time for this task.
     * @return the time or -1 if this isn't repeatable task
     */
    long getIntervalPeriod();

    boolean isRepeatable();

    /**
     * Returns the owner of this task.
     * @return the plugin
     */
    @NotNull
    BlueberryMod getOwner();

    @NotNull
    Runnable getRunnable();

    /**
     * Returns how many times the task has visited by scheduler. (Used for interval/delay)
     */
    long getCycle();
}
