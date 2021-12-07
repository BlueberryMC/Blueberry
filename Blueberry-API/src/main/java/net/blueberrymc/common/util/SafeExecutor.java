package net.blueberrymc.common.util;

import org.jetbrains.annotations.Contract;

public interface SafeExecutor<T> {
    @Contract
    T execute();
}
