package net.blueberrymc.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordRPCTaskExecutor {
    private static final String CLIENT_ID = "814409121255915520";
    private static final Logger LOGGER = LogManager.getLogger();
    private static ScheduledExecutorService executor;

    private static ScheduledExecutorService createExecutor() {
        return Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Discord RPC Task Executor").build());
    }

    private static final Queue<Runnable> taskQueue = new ArrayDeque<>();
    @Nullable
    private static Thread thread = null;
    private static boolean init = false;
    public static boolean discordRpcEnabled = false;

    @SuppressWarnings("CodeBlock2Expr")
    public static void init(boolean discordRpc) {
        if (init) return;
        init = true;
        DiscordRPCTaskExecutor.discordRpcEnabled = discordRpc;
        LOGGER.info("Discord Rich Presence: " + (discordRpcEnabled ? "Enabled" : "Disabled"));
        executor = createExecutor();
        if (discordRpcEnabled) {
            executor.scheduleAtFixedRate(() -> {
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
        } else {
            executor.scheduleAtFixedRate(() -> {
                thread = Thread.currentThread();
                taskQueue.clear();
                DiscordRPC.discordRunCallbacks();
                DiscordRichPresence presence = Blueberry.getUtil().getDiscordRichPresenceQueue();
                if (presence != null) {
                    Blueberry.getUtil().setDiscordRichPresenceQueue(null);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
        }
        DiscordRPCTaskExecutor.submit(() -> {
            LOGGER.info("Loading Discord RPC Library...");
            //noinspection InstantiationOfUtilityClass
            new DiscordRPC();
            LOGGER.info("Logging into Discord...");
            DiscordRPC.discordInitialize(CLIENT_ID, new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
                LOGGER.info("Successfully logged into Discord: " + user.username + "#" + user.discriminator + " (" + user.userId + ")");
            }).setErroredEventHandler((i, s) -> {
                LOGGER.error("Encountered error on Discord RPC: " + i + " (" + s + ")");
            }).setDisconnectedEventHandler((i, s) -> {
                LOGGER.info("Disconnected from Discord: " + i + " (" + s + ")");
            }).build(), true);
        });
    }

    public static boolean isOnExecutorThread() {
        return thread == null || !thread.isAlive() || Thread.currentThread() == thread;
    }

    public static void submit(@NotNull Runnable runnable) {
        if (isOnExecutorThread()) {
            runnable.run();
        } else {
            taskQueue.add(runnable);
        }
    }

    public static void shutdownNow() {
        if (Blueberry.getSide() != Side.CLIENT || thread == null || !thread.isAlive()) return;
        try {
            executor.shutdownNow();
            try {
                //noinspection ResultOfMethodCallIgnored
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            DiscordRPC.discordShutdown();
            thread = null;
            init = false;
            LOGGER.info("Successfully disconnected from Discord.");
        } catch (Throwable ignore) {}
    }

    public static void shutdown() {
        submit(DiscordRPCTaskExecutor::shutdownNow);
    }
}
