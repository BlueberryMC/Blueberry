package net.blueberrymc.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Simple instance of LoggedBufferedOutputStream without any protection against concurrent modification.
 */
public class SimpleLoggedBufferedOutputStream extends LoggedBufferedOutputStream {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final Logger logger;
    protected final String name;
    protected final Level level;
    private StringBuffer buf = new StringBuffer();

    public SimpleLoggedBufferedOutputStream(@NotNull String name, @NotNull Level level) {
        this(LOGGER, name, level);
    }

    protected SimpleLoggedBufferedOutputStream(@NotNull Logger logger, @NotNull String name, @NotNull Level level) {
        this.logger = logger;
        this.name = name;
        this.level = level;
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
        logger.log(level, "[{}]: {}", name, buf);
    }
}
