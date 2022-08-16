package net.blueberrymc.common.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.DiscordEventAdapter;
import de.jcm.discordgamesdk.LogLevel;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityJoinRequestReply;
import de.jcm.discordgamesdk.user.DiscordUser;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.bml.InternalBlueberryModConfig;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.util.Constants;
import net.blueberrymc.util.NumberUtil;
import net.blueberrymc.util.SystemUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * A class used to enable Discord Rich Presence.
 */
public class DiscordRPCTaskExecutor {
    private static final byte NETWORK_SYSTEM_MESSAGE_ID = 0x01;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicLong currentLobbyId = new AtomicLong(-1);
    private static ExecutorService executor;

    private static ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("Discord RPC Task Executor").build());
    }

    private static final Queue<Consumer<Core>> taskQueue = new ArrayDeque<>();
    @Nullable
    private static Thread thread = null;
    private static boolean init = false;
    private static boolean downloadedLibrary = false;
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
        if (!downloadedLibrary) {
            downloadAndLoadLibrary();
            downloadedLibrary = true;
        }
        init = true;
        DiscordRPCTaskExecutor.discordRpcEnabled = discordRpc;
        LOGGER.info("Discord Rich Presence: " + (discordRpcEnabled ? "Enabled" : "Disabled"));
        executor = createExecutor();
        taskQueue.clear();
        if (discordRpcEnabled) {
            executor.execute(() -> {
                thread = Thread.currentThread();
                LOGGER.info("Logging into Discord...");
                if (!InternalBlueberryModConfig.Misc.DiscordRPC.discordInstanceId.isEmpty()) {
                    LOGGER.info("Discord Instance ID: " + InternalBlueberryModConfig.Misc.DiscordRPC.discordInstanceId);
                }
                if (NumberUtil.isLong(InternalBlueberryModConfig.Misc.DiscordRPC.discordInstanceId)) {
                    SystemUtil.setEnvironmentVariable("DISCORD_INSTANCE_ID", InternalBlueberryModConfig.Misc.DiscordRPC.discordInstanceId);
                } else {
                    SystemUtil.setEnvironmentVariable("DISCORD_INSTANCE_ID", "");
                }
                try (CreateParams params = new CreateParams()) {
                    params.setClientID(Long.parseLong(clientId, 10));
                    params.setFlags(CreateParams.getDefaultFlags());
                    AtomicReference<Core> lateInitCore = new AtomicReference<>();
                    AtomicLong currentUserId = new AtomicLong(0);
                    params.registerEventHandler(new DiscordEventAdapter() {
                        @Override
                        public void onCurrentUserUpdate() {
                            var core = Objects.requireNonNull(lateInitCore.get());
                            var user = core.userManager().getCurrentUser();
                            currentUserId.set(user.getUserId());
                            LOGGER.info("Successfully logged into Discord: " + user.getUsername() + "#" + user.getDiscriminator() + " (" + user.getUserId() + ")");
                        }

                        @Override
                        public void onActivityJoinRequest(@NotNull DiscordUser user) {
                            var core = Objects.requireNonNull(lateInitCore.get());
                            var player = Minecraft.getInstance().player;
                            if (player == null) {
                                core.activityManager().sendRequestReply(user.getUserId(), ActivityJoinRequestReply.NO);
                                return;
                            }
                            String userTag = user.getUsername() + "#" + user.getDiscriminator();
                            var component = prefixWithDiscord();
                            component.append(BlueberryText.text("blueberry", "discord.activity.join_request.title", userTag).withStyle(ChatFormatting.WHITE));
                            component.append(new TextComponent(" - ").withStyle(ChatFormatting.GRAY));
                            component.append(
                                    BlueberryText.text("blueberry", "discord.activity.join_request.accept")
                                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                                            .withStyle(style -> {
                                                style = style.withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        BlueberryText.text("blueberry", "discord.activity.join_request.accept.hover_text")
                                                ));
                                                style = style.withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND,
                                                        "/cblueberry discord accept " + user.getUserId()
                                                ));
                                                return style;
                                            }));
                            component.append(" ");
                            component.append(
                                    BlueberryText.text("blueberry", "discord.activity.join_request.deny")
                                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                                            .withStyle(style -> {
                                                style = style.withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        BlueberryText.text("blueberry", "discord.activity.join_request.deny.hover_text")
                                                ));
                                                style = style.withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND,
                                                        "/cblueberry discord deny " + user.getUserId()
                                                ));
                                                return style;
                                            }));
                            component.append(" ");
                            component.append(
                                    BlueberryText.text("blueberry", "discord.activity.join_request.ignore")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD)
                                            .withStyle(style -> {
                                                style = style.withHoverEvent(new HoverEvent(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        BlueberryText.text("blueberry", "discord.activity.join_request.ignore.hover_text")
                                                ));
                                                style = style.withClickEvent(new ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND,
                                                        "/cblueberry discord ignore " + user.getUserId()
                                                ));
                                                return style;
                                            }));
                            player.sendMessage(component, Util.NIL_UUID);
                        }

                        @Override
                        public void onActivityJoin(@NotNull String secret) {
                            var core = Objects.requireNonNull(lateInitCore.get());
                            var lobbyManager = core.lobbyManager();
                            lobbyManager.connectLobbyWithActivitySecret(secret, (result, lobby) -> {
                                if (result != Result.OK) {
                                    LOGGER.error("Failed to join lobby with activity secret: " + result);
                                    return;
                                }
                                lobbyManager.connectNetwork(lobby);
                                lobbyManager.openNetworkChannel(lobby, NETWORK_SYSTEM_MESSAGE_ID, true);
                                var me = core.userManager().getCurrentUser();
                                var tag = me.getUsername() + "#" + me.getDiscriminator();
                                // TODO: translate
                                byte[] data = Component.Serializer.toJson(new TextComponent(tag + " joined the lobby!").withStyle(ChatFormatting.YELLOW)).getBytes(StandardCharsets.UTF_8);
                                for (DiscordUser user : lobbyManager.getMemberUsers(lobby)) {
                                    lobbyManager.sendNetworkMessage(lobby, user.getUserId(), NETWORK_SYSTEM_MESSAGE_ID, data);
                                }
                                lobbyManager.flushNetwork();
                            });
                        }

                        @Override
                        public void onNetworkMessage(long lobbyId, long userId, byte channelId, byte @NotNull [] data) {
                            if (channelId == NETWORK_SYSTEM_MESSAGE_ID) {
                                try {
                                    Component component = Component.Serializer.fromJson(new String(data));
                                    if (component != null && currentUserId.get() == userId) {
                                        var player = Minecraft.getInstance().player;
                                        if (player != null) {
                                            MutableComponent text = prefixWithDiscord();
                                            text.append(component);
                                            player.sendMessage(component, Util.NIL_UUID);
                                        }
                                    }
                                } catch (Exception e) {
                                    LOGGER.warn("Error handling network message of id " + channelId, e);
                                }
                            }
                        }
                    });
                    try (Core core = new Core(params)) {
                        lateInitCore.set(core);
                        LogLevel minLevel;
                        if (SharedConstants.IS_RUNNING_IN_IDE) {
                            minLevel = LogLevel.DEBUG;
                        } else {
                            minLevel = LogLevel.INFO;
                        }
                        core.setLogHook(minLevel, (level, msg) -> {
                            if (level == LogLevel.DEBUG) {
                                LOGGER.debug("Discord: " + msg);
                            } else if (level == LogLevel.INFO) {
                                LOGGER.info("Discord: " + msg);
                            } else if (level == LogLevel.WARN) {
                                LOGGER.warn("Discord: " + msg);
                            } else if (level == LogLevel.ERROR) {
                                LOGGER.error("Discord: " + msg);
                            }
                        });
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                // Run tasks
                                Consumer<Core> task;
                                while ((task = taskQueue.poll()) != null) {
                                    task.accept(core);
                                }
                                // Run callbacks
                                core.runCallbacks();
                                // Set activity
                                Activity activity = Blueberry.getUtil().getDiscordRichPresenceQueue();
                                if (activity != null) {
                                    String maybeLobbyIdString = activity.party().getID();
                                    if (NumberUtil.isLong(maybeLobbyIdString)) {
                                        try {
                                            long maybeLobbyId = Long.parseLong(maybeLobbyIdString);
                                            if (maybeLobbyId != -1) {
                                                var lobby = core.lobbyManager().getLobby(maybeLobbyId);
                                                activity.party().size().setCurrentSize(core.lobbyManager().memberCount(lobby));
                                                activity.party().size().setMaxSize(lobby.getCapacity());
                                                activity.secrets().setJoinSecret(core.lobbyManager().getLobbyActivitySecret(lobby));
                                            }
                                        } catch (Exception e) {
                                            LOGGER.warn("Failed to set activity party size or secrets", e);
                                        }
                                    }
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

    private static @NotNull MutableComponent prefixWithDiscord() {
        var text = new TextComponent("");
        text.append(new TextComponent("[").withStyle(ChatFormatting.BLUE));
        text.append(BlueberryText.text("blueberry", "discord.title").withStyle(ChatFormatting.BLUE));
        text.append(new TextComponent("]").withStyle(ChatFormatting.BLUE));
        text.append(" ");
        return text;
    }

    public static @NotNull CompletableFuture<@NotNull Long> createLobby() {
        if (!InternalBlueberryModConfig.Misc.DiscordRPC.enableGameInvites) {
            return CompletableFuture.completedFuture(-1L);
        }
        var future = new CompletableFuture<Long>();
        submitTask(core -> {
            var lobbyManager = core.lobbyManager();
            var transaction = core.lobbyManager().getLobbyCreateTransaction();
            transaction.setCapacity(1024);
            lobbyManager.createLobby(transaction, lobby -> {
                lobbyManager.connectNetwork(lobby);
                lobbyManager.openNetworkChannel(lobby, NETWORK_SYSTEM_MESSAGE_ID, true);
                currentLobbyId.set(lobby.getId());
                future.complete(lobby.getId());
            });
        });
        return future;
    }

    /**
     * Returns the current lobby id.
     * @return any value other than -1 if a lobby is currently open.
     */
    public static long getCurrentLobbyId() {
        return currentLobbyId.get();
    }

    public static @NotNull CompletableFuture<Result> destroyLobby() {
        if (currentLobbyId.get() == -1) {
            return CompletableFuture.completedFuture(Result.NOT_FOUND);
        }
        var future = new CompletableFuture<Result>();
        submitTask(core -> core.lobbyManager().deleteLobby(getCurrentLobbyId(), result -> {
            currentLobbyId.set(-1);
            future.complete(result);
        }));
        return future;
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
            taskQueue.add(core -> runnable.run());
        }
    }

    /**
     * Submits the task to Discord RPC Task Executor.
     * @param action the task
     */
    public static void submitTask(@NotNull Consumer<Core> action) {
        taskQueue.add(action);
    }

    /**
     * Shutdown the Discord RPC and its executor immediately.
     */
    public static void shutdownNow() {
        if (Blueberry.getSide() != Side.CLIENT || thread == null || !thread.isAlive()) return;
        try {
            try {
                destroyLobby().join();
            } catch (Exception e) {
                LOGGER.warn("Failed to delete lobby", e);
            }
            executor.shutdownNow();
            try {
                //noinspection ResultOfMethodCallIgnored
                executor.awaitTermination(10, TimeUnit.SECONDS);
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
