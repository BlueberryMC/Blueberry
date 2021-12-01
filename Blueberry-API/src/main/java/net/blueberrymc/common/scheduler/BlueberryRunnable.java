package net.blueberrymc.common.scheduler;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.NotNull;

public abstract class BlueberryRunnable implements Runnable {
    private final Side side;
    private long clientId = -1;
    private long serverId = -1;

    public BlueberryRunnable(@NotNull Side side) {
        Preconditions.checkNotNull(side, "side cannot be null");
        this.side = side;
    }

    /**
     * Get the client task associated with this runnable.
     * @throws IllegalStateException if the task isn't scheduled yet
     * @return the task
     */
    @NotNull
    public final BlueberryTask getClientTask() {
        if (clientId == -1) throw new IllegalStateException("Client-side task isn't scheduled");
        return Blueberry.getUtil().getClientScheduler().getTask(clientId);
    }

    /**
     * Get the server task associated with this runnable.
     * @throws IllegalStateException if the task isn't scheduled yet
     * @return the task
     */
    @NotNull
    public final BlueberryTask getServerTask() {
        if (serverId == -1) throw new IllegalStateException("Server-side task isn't scheduled");
        return Blueberry.getUtil().getServerScheduler().getTask(serverId);
    }

    @NotNull
    public final TaskPair runTask(@NotNull BlueberryMod blueberryMod) {
        if (side == Side.BOTH) {
            BlueberryTask clientTask = Blueberry.getUtil().getClientSchedulerOptional().map(scheduler -> scheduler.runTask(blueberryMod, this)).get();
            BlueberryTask serverTask = Blueberry.getUtil().getServerScheduler().runTask(blueberryMod, this);
            return setupId(new TaskPair(clientTask, serverTask));
        }
        return setupId(getSingleScheduler().runTask(blueberryMod, this));
    }

    @NotNull
    public final TaskPair runTaskLater(@NotNull BlueberryMod blueberryMod, long time) {
        if (side == Side.BOTH) {
            BlueberryTask clientTask = Blueberry.getUtil().getClientSchedulerOptional().map(scheduler -> scheduler.runTaskLater(blueberryMod, time, this)).get();
            BlueberryTask serverTask = Blueberry.getUtil().getServerScheduler().runTaskLater(blueberryMod, time, this);
            return setupId(new TaskPair(clientTask, serverTask));
        }
        return setupId(getSingleScheduler().runTaskLater(blueberryMod, time, this));
    }

    @NotNull
    public final TaskPair runTaskTimer(@NotNull BlueberryMod blueberryMod, long delayTime, long timerTime) {
        if (side == Side.BOTH) {
            BlueberryTask clientTask = Blueberry.getUtil().getClientSchedulerOptional().map(scheduler -> scheduler.runTaskTimer(blueberryMod, delayTime, timerTime, this)).get();
            BlueberryTask serverTask = Blueberry.getUtil().getServerScheduler().runTaskTimer(blueberryMod, delayTime, timerTime, this);
            return setupId(new TaskPair(clientTask, serverTask));
        }
        return setupId(getSingleScheduler().runTaskTimer(blueberryMod, delayTime, timerTime, this));
    }

    @NotNull
    public final TaskPair runTaskAsynchronously(@NotNull BlueberryMod blueberryMod) {
        if (side == Side.BOTH) {
            BlueberryTask clientTask = Blueberry.getUtil().getClientSchedulerOptional().map(scheduler -> scheduler.runTaskAsynchronously(blueberryMod, this)).get();
            BlueberryTask serverTask = Blueberry.getUtil().getServerScheduler().runTaskAsynchronously(blueberryMod, this);
            return setupId(new TaskPair(clientTask, serverTask));
        }
        return setupId(getSingleScheduler().runTaskAsynchronously(blueberryMod, this));
    }

    @NotNull
    public final TaskPair runTaskLaterAsynchronously(@NotNull BlueberryMod blueberryMod, long time) {
        if (side == Side.BOTH) {
            BlueberryTask clientTask = Blueberry.getUtil().getClientSchedulerOptional().map(scheduler -> scheduler.runTaskLaterAsynchronously(blueberryMod, time, this)).get();
            BlueberryTask serverTask = Blueberry.getUtil().getServerScheduler().runTaskLaterAsynchronously(blueberryMod, time, this);
            return setupId(new TaskPair(clientTask, serverTask));
        }
        return setupId(getSingleScheduler().runTaskLaterAsynchronously(blueberryMod, time, this));
    }

    @NotNull
    public final TaskPair runTaskTimerAsynchronously(@NotNull BlueberryMod blueberryMod, long delayTime, long timerTime) {
        if (side == Side.BOTH) {
            BlueberryTask clientTask = Blueberry.getUtil().getClientSchedulerOptional().map(scheduler -> scheduler.runTaskTimerAsynchronously(blueberryMod, delayTime, timerTime, this)).get();
            BlueberryTask serverTask = Blueberry.getUtil().getServerScheduler().runTaskTimerAsynchronously(blueberryMod, delayTime, timerTime, this);
            return setupId(new TaskPair(clientTask, serverTask));
        }
        return setupId(getSingleScheduler().runTaskTimerAsynchronously(blueberryMod, delayTime, timerTime, this));
    }

    @NotNull
    private AbstractBlueberryScheduler getSingleScheduler() {
        if (side == Side.CLIENT) {
            return Blueberry.getUtil().getClientScheduler();
        } else if (side == Side.SERVER) {
            return Blueberry.getUtil().getServerScheduler();
        } else {
            throw new IllegalStateException("Unsupported side: " + side.name());
        }
    }

    @NotNull
    private <T> TaskPair setupId(@NotNull T task) {
        if (task instanceof BlueberryTask blueberryTask) {
            if (side == Side.CLIENT) {
                this.clientId = blueberryTask.getTaskId();
                return new TaskPair(blueberryTask, null);
            } else if (side == Side.SERVER) {
                this.serverId = blueberryTask.getTaskId();
                return new TaskPair(null, blueberryTask);
            } else {
                throw new IllegalStateException("side: " + side.name() + ", task: " + task);
            }
        } else if (task instanceof TaskPair pair) {
            if (side == Side.BOTH) {
                this.clientId = pair.clientTask() != null ? pair.clientTask().getTaskId() : -1;
                this.serverId = pair.serverTask() != null ? pair.serverTask().getTaskId() : -1;
            } else {
                throw new IllegalStateException("side: " + side.name() + ", task: " + task);
            }
            return pair;
        } else {
            throw new IllegalArgumentException(task.getClass().getCanonicalName());
        }
    }
}
