package net.blueberrymc.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.DiscordEventAdapter;
import de.jcm.discordgamesdk.activity.Activity;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymc.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A class used to enable Discord Rich Presence.
 */
public class DiscordRPCTaskExecutor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ExecutorService executor;

    private static ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Discord RPC Task Executor").build());
    }

    private static final Queue<Runnable> taskQueue = new ArrayDeque<>();
    @Nullable
    private static Thread thread = null;
    private static boolean init = false;
    public static boolean discordRpcEnabled = false;

    public static void init(boolean discordRpc) {
        init(Constants.DISCORD_CLIENT_ID, discordRpc);
    }

    /**
     * Initializes the Discord RPC using provided client id. This method will do nothing if already initialized.
     * @param clientId client id to use
     * @param discordRpc whether to enable discord rpc (setting to false will cause Discord RPC Task Executor to do
     *                   almost nothing).
     */
    public static void init(@NotNull String clientId, boolean discordRpc) {
        if (init) return;
        downloadAndLoadLibrary();
        init = true;
        DiscordRPCTaskExecutor.discordRpcEnabled = discordRpc;
        LOGGER.info("Discord Rich Presence: " + (discordRpcEnabled ? "Enabled" : "Disabled"));
        executor = createExecutor();
        taskQueue.clear();
        if (discordRpcEnabled) {
            executor.execute(() -> {
                thread = Thread.currentThread();
                LOGGER.info("Logging into Discord...");
                try (CreateParams params = new CreateParams()) {
                    params.setClientID(Long.parseLong(clientId, 10));
                    params.setFlags(CreateParams.getDefaultFlags());
                    AtomicReference<Core> lateInitCore = new AtomicReference<>();
                    params.registerEventHandler(new DiscordEventAdapter() {
                        @Override
                        public void onCurrentUserUpdate() {
                            var core = Objects.requireNonNull(lateInitCore.get());
                            var user = core.userManager().getCurrentUser();
                            LOGGER.info("Successfully logged into Discord: " + user.getUsername() + "#" + user.getDiscriminator() + " (" + user.getUserId() + ")");
                        }
                    });
                    try (Core core = new Core(params)) {
                        lateInitCore.set(core);
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                // Run tasks
                                Runnable task;
                                while ((task = taskQueue.poll()) != null) {
                                    task.run();
                                }
                                // Run callbacks
                                core.runCallbacks();
                                // Set activity
                                Activity activity = Blueberry.getUtil().getDiscordRichPresenceQueue();
                                if (activity != null) {
                                    LOGGER.info("Updating Discord Rich Presence to State: " + activity.getState() + ", Details: " + activity.getDetails());
                                    core.activityManager().updateActivity(activity);
                                    Blueberry.getUtil().setDiscordRichPresenceQueue(null);
                                }
                            } catch (Exception e) {
                                LOGGER.error(e);
                            }
                            // Sleep
                            try {
                                //noinspection BusyWait
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }
                }

            });
        }
    }

    /**
     * Checks if current thread is on executor thread.
     * @return true if running in executor thread, false if executor thread is not running or running in different thread
     */
    public static boolean isOnExecutorThread() {
        return thread != null && thread.isAlive() && Thread.currentThread() == thread;
    }

    /**
     * Submits the task to Discord RPC Task Executor.
     * @param runnable the task
     */
    public static void submit(@NotNull Runnable runnable) {
        if (isOnExecutorThread()) {
            runnable.run();
        } else {
            taskQueue.add(runnable);
        }
    }

    /**
     * Shutdown the Discord RPC and its executor immediately.
     */
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
            thread = null;
            init = false;
            LOGGER.info("Successfully disconnected from Discord.");
        } catch (Throwable ignore) {}
    }

    /**
     * Shutdown the Discord RPC and its executor asynchronously.
     */
    public static void shutdown() {
        submit(DiscordRPCTaskExecutor::shutdownNow);
    }

    private static void downloadAndLoadLibrary() {
        LOGGER.info("Downloading Discord GameSDK...");
        try {
            Core.initDownload();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download Discord GameSDK", e);
        }
    }
}
