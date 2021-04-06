package net.blueberrymc.util;

import javax.annotation.Nullable;
import java.io.PrintStream;

public class NoopPrintStream extends PrintStream {
   public NoopPrintStream() {
      super(new NoopOutputStream());
   }

   public void println(@Nullable String s) {}

   public void println(@Nullable Object object) {}
}