package net.blueberrymc.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public abstract class LoggedBufferedOutputStream extends OutputStream {
    protected static final String LINE_SEPARATOR = System.lineSeparator();

    @NotNull
    protected abstract String getBuffer();

    protected abstract void appendBuffer(@NotNull Object o);

    protected abstract void setBuffer(@NotNull String buf);

    protected abstract void log(@NotNull String buf);

    @Override
    public void write(int i) throws IOException {
        // append to buffer and flush if necessary
        appendBuffer(String.valueOf((char) i));
        //setBuffer(getBuffer() + new String(new byte[]{(byte) (i & 0xff)}));
        if (getBuffer().endsWith(LINE_SEPARATOR) || getBuffer().endsWith("\n")) {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        String buf = getBuffer();
        if (buf.trim().isBlank()) {
            return;
        }
        log(buf.substring(0, buf.length() - 1));
        setBuffer("");
    }
}
