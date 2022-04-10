package net.blueberrymc.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Thread local instance of LoggedBufferedOutputStream. Internal buffer is wrapped with ThreadLocal.
 */
public class ThreadLocalLoggedBufferedOutputStream extends SimpleLoggedBufferedOutputStream {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ThreadLocal<StringBuffer> buf = ThreadLocal.withInitial(StringBuffer::new);

    public ThreadLocalLoggedBufferedOutputStream(@NotNull String name, @NotNull Level level) {
        super(LOGGER, name, level);
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
