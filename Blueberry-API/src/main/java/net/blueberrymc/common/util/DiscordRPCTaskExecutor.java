package net.blueberrymc.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.blueberrymc.common.Blueberry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordRPCTaskExecutor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Discord RPC Task Executor").build());
    private static final Queue<Runnable> taskQueue = new ArrayDeque<>();
    private static Thread thread = null;
    private static boolean init = false;
    public static boolean discordRpcEnabled = false;

    @SuppressWarnings("CodeBlock2Expr")
    public static void init(boolean discordRpc) {
        if (init) return;
        init = true;
        DiscordRPCTaskExecutor.discordRpcEnabled = discordRpc;
        LOGGER.info("Discord RPC: " + (discordRpcEnabled ? "Enabled" : "Disabled"));
        if (discordRpcEnabled) {
            EXECUTOR.scheduleAtFixedRate(() -> {
                thread = Thread.currentThread();
                Runnable task;
                while ((task = taskQueue.poll()) != null) {
                    task.run();
                }
                DiscordRPC.discordRunCallbacks();
                DiscordRichPresence presence = Blueberry.getUtil().getDiscordRichPresenceQueue();
                if (presence != null) {
                    LOGGER.info("Updating Discord Rich Presence to State: " + presence.state + ", Details: " + presence.details);
                    DiscordRPC.discordUpdatePresence(presence);
                    Blueberry.getUtil().setDiscordRichPresenceQueue(null);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
        }
        DiscordRPCTaskExecutor.submit(() -> {
            LOGGER.info("Loading Discord RPC Library...");
            //noinspection InstantiationOfUtilityClass
            new DiscordRPC();
            LOGGER.info("Logging into Discord...");
            DiscordRPC.discordInitialize("814409121255915520", new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
                LOGGER.info("Successfully logged into Discord: " + user.username + "#" + user.discriminator + " (" + user.userId + ")");
            }).setErroredEventHandler((i, s) -> {
                LOGGER.error("Encountered error on Discord RPC: " + i + " (" + s + ")");
            }).setDisconnectedEventHandler((i, s) -> {
                LOGGER.warn("Disconnected from Discord: " + i + " (" + s + ")");
            }).build(), true);
        });
    }

    public static boolean isOnExecutorThread() {
        return Thread.currentThread().equals(thread);
    }

    public static void submit(Runnable runnable) {
        if (isOnExecutorThread()) {
            runnable.run();
        } else {
            taskQueue.add(runnable);
        }
    }

    public static void shutdownNow() {
        try {
            DiscordRPC.discordShutdown();
            LOGGER.info("Successfully disconnected from Discord.");
        } catch (Throwable ignore) {}
    }

    public static void shutdown() {
        submit(DiscordRPCTaskExecutor::shutdownNow);
    }
}
