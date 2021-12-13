package net.blueberrymc.common.util;

import org.jetbrains.annotations.Contract;

public abstract class SafeExecutor<T> {
    @Contract
    public abstract T execute();
}
