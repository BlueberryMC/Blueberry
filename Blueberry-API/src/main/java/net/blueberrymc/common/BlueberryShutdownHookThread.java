package net.blueberrymc.common;

import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.blueberrymc.common.util.FileUtil;
import net.blueberrymc.server.main.ServerMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

class BlueberryShutdownHookThread extends Thread {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean executed = false;

    BlueberryShutdownHookThread() {
        super("Blueberry Shutdown Hook Thread");
    }

    @Override
    public void run() {
        if (executed) return; // Don't execute more than once
        executed = true;
        LOGGER.info("Shutting down Discord RPC Task Executor");
        DiscordRPCTaskExecutor.shutdownNow();
        if (ServerMain.tempModDir != null) {
            LOGGER.info("Deleting temp directory " + ServerMain.tempModDir.getAbsolutePath());
            try {
                FileUtil.delete(ServerMain.tempModDir);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete temp directory " + ServerMain.tempModDir, e);
            }
        }
    }
}
