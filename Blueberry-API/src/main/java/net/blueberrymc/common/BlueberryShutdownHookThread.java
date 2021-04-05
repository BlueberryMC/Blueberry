package net.blueberrymc.common;

import net.blueberrymc.common.util.DiscordRPCTaskExecutor;

class BlueberryShutdownHookThread extends Thread {
    BlueberryShutdownHookThread() {
        super("Blueberry Shutdown Hook Thread");
    }

    @Override
    public void run() {
        DiscordRPCTaskExecutor.shutdownNow();
    }
}
