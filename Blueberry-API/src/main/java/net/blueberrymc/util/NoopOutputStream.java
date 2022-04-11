package net.blueberrymc.util;

import java.io.OutputStream;

public class NoopOutputStream extends OutputStream {
    @Override
    public void write(int b) {
    }
}
