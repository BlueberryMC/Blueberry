package net.blueberrymc.common;

import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class BlueberryShutdownHookThread extends Thread {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean executed = false;

    BlueberryShutdownHookThread() {
        super("Blueberry Shutdown Hook Thread");
    }

    @Override
    public void run() {
        if (executed) return; // Don't execute more than 1 times
        executed = true;
        DiscordRPCTaskExecutor.shutdownNow();
    }
}
