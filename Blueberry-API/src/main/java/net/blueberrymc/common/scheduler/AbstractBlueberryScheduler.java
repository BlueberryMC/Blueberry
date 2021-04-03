package net.blueberrymc.common.scheduler;

import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.util.ThrowableRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Scheduler for Blueberry.<br />
 * There are few important notes:<br />
 * <ul>
 *     <li>Server-side async tasks are run every 50 ms. (aka. 1 tick)</li>
 *     <li>Client-side async tasks are run every 1 ms.</li>
 * </ul>
 */
public abstract class AbstractBlueberryScheduler {
    protected static final Logger LOGGER = LogManager.getLogger("Blueberry Scheduler");
    protected static final Logger ASYNC_LOGGER = LogManager.getLogger("Async Blueberry Scheduler");
    protected final AtomicLong nextId = new AtomicLong();
    protected final Map<Long, BlueberryTask> tasks = new ConcurrentHashMap<>();
    protected long tickCount = 0L;
    protected long asyncTickCount = 0L;

    public void tick() {
        // increase tick count
        tasks.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isSync())
                .forEach(entry -> ((ScheduledBlueberryTask) entry.getValue()).cycle.incrementAndGet());
        // run tasks and remove if it isn't repeatable
        tasks.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isSync() && !((ScheduledBlueberryTask) entry.getValue()).executedDelayedTask)
                .filter(entry -> entry.getValue().getDelayTime() == -1 || ((ScheduledBlueberryTask) entry.getValue()).cycle.get() % entry.getValue().getDelayTime() == 0)
                .forEach(entry -> {
                    ((ScheduledBlueberryTask) entry.getValue()).executedDelayedTask = true;
                    catchSync(() -> entry.getValue().getRunnable().run());
                    if (!entry.getValue().isRepeatable()) tasks.remove(entry.getKey());
                });
        // run tasks and remove if it isn't repeatable
        tasks.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isSync() && entry.getValue().isRepeatable() && ((ScheduledBlueberryTask) entry.getValue()).executedDelayedTask)
                .filter(entry -> entry.getValue().getIntervalPeriod() != -1 && (((ScheduledBlueberryTask) entry.getValue()).cycle.get() - Math.max(entry.getValue().getDelayTime(), 0)) % entry.getValue().getIntervalPeriod() == 0)
                .forEach(entry -> catchSync(() -> entry.getValue().getRunnable().run()));
        tickCount++;
    }

    public void tickAsync() {
        // increase tick count
        tasks.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAsync())
                .forEach(entry -> ((ScheduledBlueberryTask) entry.getValue()).cycle.incrementAndGet());
        // run tasks and remove if it isn't repeatable
        tasks.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAsync() && !((ScheduledBlueberryTask) entry.getValue()).executedDelayedTask)
                .filter(entry -> entry.getValue().getDelayTime() == -1 || ((ScheduledBlueberryTask) entry.getValue()).cycle.get() % entry.getValue().getDelayTime() == 0)
                .forEach(entry -> {
                    ((ScheduledBlueberryTask) entry.getValue()).executedDelayedTask = true;
                    catchAsync(() -> entry.getValue().getRunnable().run());
                    if (!entry.getValue().isRepeatable()) tasks.remove(entry.getKey());
                });
        // run tasks and remove if it isn't repeatable
        tasks.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAsync() && entry.getValue().isRepeatable() && ((ScheduledBlueberryTask) entry.getValue()).executedDelayedTask)
                .filter(entry -> entry.getValue().getIntervalPeriod() != -1 && (((ScheduledBlueberryTask) entry.getValue()).cycle.get() - Math.max(entry.getValue().getDelayTime(), 0)) % entry.getValue().getIntervalPeriod() == 0)
                .forEach(entry -> catchAsync(() -> entry.getValue().getRunnable().run()));
        asyncTickCount++;
    }

    /**
     * Gets the specific task by their id.
     * @param id task id
     * @return the task
     * @throws IllegalArgumentException Thrown when there are no task associated with an id.
     */
    @NotNull
    public BlueberryTask getTask(long id) {
        BlueberryTask task = tasks.get(id);
        if (task == null) throw new IllegalArgumentException("Could not find task by id " + id);
        return task;
    }

    @NotNull
    public BlueberryTask runTask(@NotNull BlueberryMod mod, @NotNull Runnable task) {
        long id = nextId.getAndIncrement();
        ScheduledBlueberryTask blueberryTask = new ScheduledBlueberryTask(this, id, mod, task, true, false, -1, -1);
        tasks.put(id, blueberryTask);
        return blueberryTask;
    }

    @NotNull
    public BlueberryTask runTaskLater(@NotNull BlueberryMod mod, @NotNull Runnable task, long delay) {
        if (delay < 1) throw new IllegalArgumentException("delay time is lower than 1");
        long id = nextId.getAndIncrement();
        ScheduledBlueberryTask blueberryTask = new ScheduledBlueberryTask(this, id, mod, task, true, false, delay, -1);
        tasks.put(id, blueberryTask);
        return blueberryTask;
    }

    @NotNull
    public BlueberryTask runTaskTimer(@NotNull BlueberryMod mod, @NotNull Runnable task, long delay, long interval) {
        if (delay < 1) throw new IllegalArgumentException("delay time is lower than 1");
        if (interval < 1) throw new IllegalArgumentException("interval period is lower than 1");
        long id = nextId.getAndIncrement();
        ScheduledBlueberryTask blueberryTask = new ScheduledBlueberryTask(this, id, mod, task, true, true, delay, interval);
        tasks.put(id, blueberryTask);
        return blueberryTask;
    }

    @NotNull
    public BlueberryTask runTaskAsynchronously(@NotNull BlueberryMod mod, @NotNull Runnable task) {
        long id = nextId.getAndIncrement();
        ScheduledBlueberryTask blueberryTask = new ScheduledBlueberryTask(this, id, mod, task, false, false, -1, -1);
        tasks.put(id, blueberryTask);
        return blueberryTask;
    }

    @NotNull
    public BlueberryTask runTaskLaterAsynchronously(@NotNull BlueberryMod mod, @NotNull Runnable task, long delay) {
        if (delay < 1) throw new IllegalArgumentException("delay time is lower than 1");
        long id = nextId.getAndIncrement();
        ScheduledBlueberryTask blueberryTask = new ScheduledBlueberryTask(this, id, mod, task, false, false, delay, -1);
        tasks.put(id, blueberryTask);
        return blueberryTask;
    }

    @NotNull
    public BlueberryTask runTaskTimerAsynchronously(@NotNull BlueberryMod mod, @NotNull Runnable task, long delay, long interval) {
        if (delay < 1) throw new IllegalArgumentException("delay time is lower than 1");
        if (interval < 1) throw new IllegalArgumentException("interval period is lower than 1");
        long id = nextId.getAndIncrement();
        ScheduledBlueberryTask blueberryTask = new ScheduledBlueberryTask(this, id, mod, task, false, true, delay, interval);
        tasks.put(id, blueberryTask);
        return blueberryTask;
    }

    /**
     * Cancels the specific task.
     * @param id task id
     * @throws IllegalArgumentException Thrown when there are no task associated with an id.
     */
    public void cancelTask(long id) throws IllegalArgumentException {
        getTask(id).cancel();
    }

    /**
     * @deprecated internal usage only
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    @Nullable
    public BlueberryTask removeTask(long id) {
        return tasks.remove(id);
    }

    public long getTickCount() {
        return tickCount;
    }

    public long getAsyncTickCount() {
        return asyncTickCount;
    }

    protected static void catchSync(@NotNull ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            LOGGER.warn(e);
        }
    }

    protected static void catchAsync(@NotNull ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            ASYNC_LOGGER.warn(e);
        }
    }
}
