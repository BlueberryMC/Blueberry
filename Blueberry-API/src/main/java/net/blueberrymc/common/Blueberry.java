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
import net.blueberrymc.common.bml.InternalBlueberryMod;
import net.blueberrymc.common.util.BlueberryVersion;
import net.blueberrymc.common.util.DiscordRPCTaskExecutor;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.server.BlueberryServer;
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
    public static boolean stopping = false;

    @Contract(pure = true)
    @NotNull
    public static ModLoader getModLoader() {
        return modLoader;
    }

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

    @Contract(pure = true)
    @NotNull
    public static Side getSide() {
        return side;
    }

    @Contract(pure = true)
    @NotNull
    public static File getGameDir() {
        return gameDir;
    }

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

    @Contract(pure = true)
    @NotNull
    public static BlueberryUtil getUtil() {
        return util;
    }

    @NotNull
    public static ModState getCurrentState() {
        return Objects.requireNonNull(getModManager().getModById("blueberry")).getStateList().getCurrentState();
    }

    public static void bootstrap(@NotNull Side side, @NotNull File gameDir, @NotNull BlueberryUtil utilImpl) {
        Preconditions.checkArgument(Blueberry.side == null && util == null, "Blueberry is already initialized!");
        Preconditions.checkArgument(side != Side.BOTH, "Invalid Side: " + side.name());
        Preconditions.checkNotNull(gameDir, "gameDir cannot be null");
        Runtime.getRuntime().addShutdownHook(new BlueberryShutdownHookThread());
        Blueberry.side = side;
        Blueberry.gameDir = gameDir;
        if (isClient()) {
            util = new BlueberryClient((BlueberryClient) utilImpl);
        } else if (isServer()) {
            util = new BlueberryServer((BlueberryServer) utilImpl);
        } else {
            util = new BlueberryNope();
        }
        try {
            modLoader = new BlueberryModLoader();
            BlueberryVersion version = getVersion();
            LOGGER.info("Loading " + name + " version " + version.getFullyQualifiedVersion() + " (" + getSide().getName() + ")");
            registerInternalMod();
            if (isClient()) new EarlyLoadingScreen().startRender(true);
            //MixinBootstrap.init();
            Blueberry.getModLoader().loadMods();
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

    @SuppressWarnings("deprecation")
    private static void registerInternalMod() {
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
        Blueberry.getModLoader().forceRegisterMod(description, InternalBlueberryMod.class, false);
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
        crash(CrashReport.forThrowable(throwable, message));
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

    /* Constructor to prevent creating instance of this class */
    @Contract(value = " -> fail", pure = true)
    private Blueberry() {
        throw new IllegalStateException();
    }
}
