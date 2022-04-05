package net.blueberrymc.common.scheduler;

import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;

public class ScheduledBlueberryTask implements BlueberryTask {
    private final AbstractBlueberryScheduler scheduler;
    private final long taskId;
    private final BlueberryMod owner;
    private final Runnable runnable;
    private final boolean sync;
    private final boolean repeatable;
    private final long delay;
    private final long intervalPeriod;
    private boolean cancelled = false;
    boolean executedDelayedTask = false;
    final AtomicLong cycle = new AtomicLong();

    public ScheduledBlueberryTask(
            @NotNull AbstractBlueberryScheduler scheduler,
            long taskId,
            @NotNull BlueberryMod mod,
            @NotNull Runnable runnable,
            boolean sync,
            boolean repeatable,
            long delay,
            long intervalPeriod) {
        this.scheduler = scheduler;
        this.taskId = taskId;
        this.owner = mod;
        this.runnable = runnable;
        this.sync = sync;
        this.repeatable = repeatable;
        this.delay = delay;
        this.intervalPeriod = intervalPeriod;
    }

    @NotNull
    @Override
    public AbstractBlueberryScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void cancel() {
        cancelled = true;
        scheduler.removeTask(taskId);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public long getTaskId() {
        return taskId;
    }

    @Override
    public boolean isSync() {
        return sync;
    }

    @Override
    public long getDelayTime() {
        return delay;
    }

    @Override
    public long getIntervalPeriod() {
        return intervalPeriod;
    }

    @Override
    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public @NotNull BlueberryMod getOwner() {
        return owner;
    }

    @NotNull
    @Override
    public Runnable getRunnable() {
        return runnable;
    }

    @Override
    public long getCycle() {
        return cycle.get();
    }

    @Contract(value = "null -> false", pure = true)
    public boolean equals(@Nullable Object o) {
        if (o == null) return false;
        if (!(o instanceof BlueberryTask)) return false;
        return ((BlueberryTask) o).getTaskId() == this.taskId;
    }

    @Override
    public String toString() {
        return "ScheduledBlueberryTask{" + "taskId=" + taskId +
                ", owner=" + owner +
                ", runnable=" + runnable +
                ", sync=" + sync +
                ", repeatable=" + repeatable +
                ", delay=" + delay +
                ", repeat=" + intervalPeriod +
                ", cancelled=" + cancelled +
                ", cycle=" + cycle +
                '}';
    }
}
