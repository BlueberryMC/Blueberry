package net.blueberrymc.gradle.buildSrc.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Thread local instance of LoggedBufferedOutputStream. Internal buffer is wrapped with ThreadLocal.
 */
public class ThreadLocalLoggedBufferedOutputStream extends SimpleLoggedBufferedOutputStream {
    private final ThreadLocal<StringBuffer> buf = ThreadLocal.withInitial(StringBuffer::new);

    public ThreadLocalLoggedBufferedOutputStream(@NotNull Logger logger, @NotNull String name) {
        super(logger, name);
    }

    @Override
    protected @NotNull String getBuffer() {
        return this.buf.get().toString();
    }

    @Override
    protected void appendBuffer(@NotNull Object o) {
        this.buf.get().append(o);
    }

    @Override
    protected void setBuffer(@NotNull String buf) {
        this.buf.set(new StringBuffer(buf));
    }
}
