package net.blueberrymc.common.scheduler;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.NotNull;

public abstract class ForwardingBlueberryTask implements BlueberryTask {
    @NotNull
    public abstract BlueberryTask delegate();

    @NotNull
    @Override
    public AbstractBlueberryScheduler getScheduler() {
        return delegate().getScheduler();
    }

    @Override
    public void cancel() { delegate().cancel(); }

    @Override
    public boolean isCancelled() { return delegate().isCancelled(); }

    @Override
    public long getTaskId() { return delegate().getTaskId(); }

    @Override
    public boolean isSync() { return delegate().isSync(); }

    @Override
    public long getDelayTime() { return delegate().getDelayTime(); }

    @Override
    public long getIntervalPeriod() { return delegate().getIntervalPeriod(); }

    @Override
    public boolean isRepeatable() { return delegate().isRepeatable(); }

    @Override
    public @NotNull BlueberryMod getOwner() { return delegate().getOwner(); }

    @Override
    public @NotNull Runnable getRunnable() { return delegate().getRunnable(); }

    @Override
    public long getCycle() { return delegate().getCycle(); }

    @NotNull
    public static ForwardingBlueberryTask getInstance(@NotNull BlueberryTask task) {
        Preconditions.checkNotNull(task, "task cannot be null");
        return new ForwardingBlueberryTask() {
            @Override
            public @NotNull BlueberryTask delegate() {
                return task;
            }
        };
    }

    @Override
    public String toString() {
        return delegate().toString();
    }
}
