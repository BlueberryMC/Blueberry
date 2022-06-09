package net.blueberrymc.util;

public interface ThrowableRunnableX<X extends Throwable> {
    void run() throws X;
}
