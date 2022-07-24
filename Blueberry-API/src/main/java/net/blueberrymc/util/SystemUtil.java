package net.blueberrymc.util;

import com.sun.jna.platform.unix.LibC;
import com.sun.jna.platform.win32.Kernel32;
import org.jetbrains.annotations.NotNull;

public class SystemUtil {
    /**
     * Sets the <i>native</i> environment variable. Does not affect the value of {@link System#getenv()}.
     * @param key The key of the environment variable.
     * @param value The value of the environment variable.
     */
    public static void setEnvironmentVariable(@NotNull String key, @NotNull String value) {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            Kernel32.INSTANCE.SetEnvironmentVariable(key, value);
        } else {
            LibC.INSTANCE.setenv(key, value, 1);
        }
    }
}
