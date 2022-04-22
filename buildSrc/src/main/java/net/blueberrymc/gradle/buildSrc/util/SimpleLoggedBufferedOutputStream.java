package net.blueberrymc.gradle.buildSrc.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Simple instance of LoggedBufferedOutputStream without any protection against concurrent modification.
 */
public class SimpleLoggedBufferedOutputStream extends LoggedBufferedOutputStream {
    protected final Logger logger;
    protected final String name;
    private StringBuffer buf = new StringBuffer();

    protected SimpleLoggedBufferedOutputStream(@NotNull Logger logger, @NotNull String name) {
        this.logger = logger;
        this.name = name;
    }

    @Override
    protected @NotNull String getBuffer() {
        return this.buf.toString();
    }

    @Override
    protected void appendBuffer(@NotNull Object o) {
        buf.append(o);
    }

    @Override
    protected void setBuffer(@NotNull String buf) {
        this.buf = new StringBuffer(buf);
    }

    @Override
    protected void log(@NotNull String buf) {
        logger.warn("[{}]: {}", name, buf);
    }
}
