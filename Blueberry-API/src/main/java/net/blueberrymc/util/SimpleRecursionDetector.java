package net.blueberrymc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleRecursionDetector {
    public static final boolean ENABLED = Boolean.getBoolean("blueberry.recursion.detector.enabled");
    private final ThreadLocal<AtomicBoolean> holding;

    public SimpleRecursionDetector() {
        if (ENABLED) {
            holding = ThreadLocal.withInitial(() -> new AtomicBoolean(false));
        } else {
            holding = null;
        }
    }

    @Contract(pure = true)
    public <X extends Throwable> void invoke(@NotNull ThrowableRunnableX<X> runnable) throws X {
        push();
        try {
            runnable.run();
        } finally {
            pop();
        }
    }

    public void push() {
        if (!ENABLED) {
            return;
        }
        assert holding != null;
        if (!holding.get().compareAndSet(false, true)) {
            throw new RuntimeException("recursion");
        }
    }

    public void pop() {
        if (!ENABLED) {
            return;
        }
        assert holding != null;
        if (!holding.get().compareAndSet(true, false)) {
            throw new IllegalStateException("Tried to pop nothing");
        }
    }
}
