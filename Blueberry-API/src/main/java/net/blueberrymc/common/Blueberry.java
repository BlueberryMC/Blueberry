package net.blueberrymc.common;

import com.google.common.base.Preconditions;
import net.blueberrymc.client.BlueberryClient;
import net.blueberrymc.client.EarlyLoadingScreen;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.BlueberryModLoader;
import net.blueberrymc.common.bml.event.EventManager;
import net.blueberrymc.common.bml.ModLoader;
import net.blueberrymc.common.bml.ModManager;
import net.blueberrymc.common.bml.ModState;
import net.blueberrymc.common.util.BlueberryVersion;
import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.blueberrymc.common.util.SafeExecutor;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.common.util.VoidSafeExecutor;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.server.BlueberryServer;
import net.blueberrymc.server.main.ServerMain;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Blueberry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String name = "Blueberry";
    private static BlueberryModLoader modLoader;
    private static final EventManager eventManager = new EventManager();
    private static final ModManager modManager = new ModManager();
    private static Side side;
    private static BlueberryUtil util;
    private static File gameDir;

    /**
     * Do not change the value please
     */
    //@Deprecated
    public static boolean stopping = false;

    @Contract(pure = true)
    @NotNull
    public static ModLoader getModLoader() {
        return modLoader;
    }

    /**
     * Returns the blueberry version
     * @return blueberry version
     */
    @Contract(pure = true)
    @NotNull
    public static BlueberryVersion getVersion() {
        return Versioning.getVersion();
    }

    @Contract(pure = true)
    @NotNull
    public static EventManager getEventManager() {
        return eventManager;
    }

    @Contract(pure = true)
    @NotNull
    public static ModManager getModManager() {
        return modManager;
    }

    /**
     * Returns the "physical" side of Blueberry. If called on integrated server which is included in client, this method
     * would return <code>CLIENT</code>. However, if called on dedicated server, this method would return
     * <code>SERVER</code>. In short, if you've used
     * @return the "physical" side of Blueberry
     */
    @Contract(pure = true)
    @NotNull
    public static Side getSide() {
        return side;
    }

    /**
     * Returns the game directory (universe in server)
     * @return game (root) directory
     */
    @Contract(pure = true)
    @NotNull
    public static File getGameDir() {
        return gameDir;
    }

    /**
     * Returns the logs/latest.log file.
     * @return latest.log file
     */
    @Contract(" -> new")
    @NotNull
    public static File getLogFile() {
        return new File(gameDir, "logs/latest.log");
    }

    @Contract(pure = true)
    @NotNull
    public static File getModsDir() {
        return modLoader.getModsDir();
    }

    @Contract(pure = true)
    @NotNull
    public static File getConfigDir() {
        return modLoader.getConfigDir();
    }

    @Contract(pure = true)
    public static boolean isClient() {
        return side == Side.CLIENT;
    }

    @Contract(pure = true)
    public static boolean isServer() {
        return side == Side.SERVER;
    }

    @Nullable
    public static <T> T getOnClient(@NotNull Supplier<T> supplier) {
        if (side == Side.CLIENT) return supplier.get();
        return null;
    }

    @Nullable
    public static <T> T getOnServer(@NotNull Supplier<T> supplier) {
        if (side == Side.SERVER) return supplier.get();
        return null;
    }

    public static void runOnClient(@NotNull Runnable runnable) {
        if (side == Side.CLIENT) runnable.run();
    }

    public static void runOnServer(@NotNull Runnable runnable) {
        if (side == Side.SERVER) runnable.run();
    }

    @Nullable
    public static <T> T safeGetOnClient(@NotNull Supplier<SafeExecutor<T>> supplier) {
        if (side == Side.CLIENT) return supplier.get().execute();
        return null;
    }

    @Nullable
    public static <T> T safeGetOnServer(@NotNull Supplier<SafeExecutor<T>> supplier) {
        if (side == Side.SERVER) return supplier.get().execute();
        return null;
    }

    public static void safeRunOnClient(@NotNull Supplier<VoidSafeExecutor> runnable) {
        if (side == Side.CLIENT) runnable.get().execute();
    }

    public static void safeRunOnServer(@NotNull Supplier<VoidSafeExecutor> runnable) {
        if (side == Side.SERVER) runnable.get().execute();
    }

    /**
     * Returns the side-dependant utility class which contains the side-specific methods.
     * @return side-dependant utility class
     */
    @Contract(pure = true)
    @NotNull
    public static BlueberryUtil getUtil() {
        return Objects.requireNonNull(util);
    }

    /**
     * Returns the mod state of blueberry mod. It should reflect the current state of the game.
     * @return current state
     */
    @NotNull
    public static ModState getCurrentState() {
        BlueberryMod mod = getModManager().getModById("blueberry");
        if (mod != null) mod.getStateList().getCurrentState();
        return ModState.LOADED;
    }

    /**
     * Pre-bootstrap to load some classes.
     */
    public static void preBootstrap() {
        Blueberry.side = Side.valueOf((String) Objects.requireNonNull(ServerMain.blackboard.get("side"), "side is null"));
        Blueberry.gameDir = (File) Objects.requireNonNull(ServerMain.blackboard.get("universe"), "universe is null");
        modLoader = new BlueberryModLoader();
    }

    public static void bootstrap(@Nullable BlueberryUtil utilImpl) {
        Preconditions.checkArgument(util == null, "Blueberry is already initialized!");
        Preconditions.checkArgument(side != null && gameDir != null && modLoader != null, "Blueberry#preBootstrap was not called");
        if (Boolean.parseBoolean(ServerMain.blackboard.get("debug").toString())) SharedConstants.IS_RUNNING_IN_IDE = true;
        Runtime.getRuntime().addShutdownHook(new BlueberryShutdownHookThread());
        if (isClient()) {
            util = safeGetOnClient(() -> new SafeExecutor<>() {
                @NotNull
                @Override
                public BlueberryUtil execute() {
                    return new BlueberryClient((BlueberryClient) utilImpl);
                }
            });
        } else if (isServer()) {
            util = new BlueberryServer((BlueberryServer) utilImpl);
        } else {
            util = new BlueberryNope();
        }
        if (util == null) throw new AssertionError("util is null");
        try {
            BlueberryVersion version = getVersion();
            LOGGER.info("Loading " + name + " version " + version.getFullyQualifiedVersion() + " (" + getSide().getName() + ")");
            registerInternalMod();
            if (isClient()) new EarlyLoadingScreen().startRender(true);
            modLoader.loadMods();
            LOGGER.info("Loaded " + Blueberry.getModLoader().getLoadedMods().size() + " mods");
        } catch (Throwable throwable) {
            crash(throwable, "Initializing Blueberry");
        }
    }

    public static void shutdown() {
        if (stopping) return;
        stopping = true;
        LOGGER.info("Shutting down Discord RPC");
        DiscordRPCTaskExecutor.shutdownNow();
        LOGGER.info("Disabling mods");
        List<BlueberryMod> mods = new ArrayList<>(modLoader.getLoadedMods());
        Collections.reverse(mods);
        mods.forEach(modLoader::disableMod);
    }

    /**
     * Crashes the Minecraft with provided crash report.
     * @param crashReport the crash report
     */
    public static void crash(@NotNull CrashReport crashReport) {
        getUtil().crash(crashReport);
    }

    /**
     * Crashes the minecraft with a throwable and a message.
     * @param throwable throwable (generates stack trace section from it)
     * @param message the message (description)
     */
    public static void crash(@NotNull Throwable throwable, @NotNull String message) {
        try {
            crash(CrashReport.forThrowable(throwable, message));
        } catch (Throwable throwable1) {
            LOGGER.fatal("Crashed while crashing the minecraft", throwable1);
            LOGGER.fatal("Provided throwable: ", throwable);
            throw new RuntimeException();
        }
    }

    /**
     * Copy of {@link net.minecraft.Util#pauseInIde(Throwable)}.
     */
    @Contract("_ -> param1")
    public static <T extends Throwable> T pauseInIde(@NotNull T throwable) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", throwable);
            doPause();
        }
        return throwable;
    }

    private static void doPause() {
        while (true) {
            try {
                //noinspection BusyWait
                Thread.sleep(1000L);
                LOGGER.error("paused");
            } catch (InterruptedException var1) {
                return;
            }
        }
    }

    static void registerInternalMod() {
        ModDescriptionFile description = new ModDescriptionFile(
                "blueberry",
                Versioning.getVersion().getFullyQualifiedVersion(),
                "net.blueberrymc.common.bml.InternalBlueberryMod",
                "Blueberry",
                Collections.singletonList("Blueberry development team"),
                Collections.singletonList("MagmaCube"),
                Collections.singletonList("Modding API for Minecraft"),
                false,
                null,
                null,
                false,
                null,
                null);
        ((BlueberryModLoader) Blueberry.getModLoader()).registerInternalBlueberryMod(description);
    }

    /* Constructor to prevent creating instance of this class */
    @Contract(value = " -> fail", pure = true)
    private Blueberry() {
        throw new IllegalStateException();
    }
}
