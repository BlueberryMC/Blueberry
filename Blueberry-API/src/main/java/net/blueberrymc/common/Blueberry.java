package net.blueberrymc.common;

import com.google.common.base.Preconditions;
import net.blueberrymc.client.BlueberryClient;
import net.blueberrymc.common.bml.BlueberryModLoader;
import net.blueberrymc.common.bml.EventManager;
import net.blueberrymc.common.bml.ModLoader;
import net.blueberrymc.common.bml.ModManager;
import net.blueberrymc.common.bml.mod.InternalBlueberryMod;
import net.blueberrymc.common.util.BlueberryVersion;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.server.BlueberryServer;
import net.minecraft.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Blueberry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String name = "Blueberry";
    private static BlueberryModLoader modLoader;
    private static final EventManager eventManager = new EventManager();
    private static final ModManager modManager = new ModManager();
    private static Side side;
    private static BlueberryUtil util;
    private static File gameDir;

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

    @Contract(pure = true)
    @NotNull
    public static BlueberryUtil getUtil() {
        if (util == null) {
            if (isClient()) {
                util = new BlueberryClient();
            } else if (isServer()) {
                util = new BlueberryServer();
            } else {
                util = new BlueberryNope();
            }
        }
        return util;
    }

    public static void bootstrap(@NotNull("side") Side side, @NotNull("gameDir") File gameDir) {
        Preconditions.checkArgument(Blueberry.side == null, "Blueberry is already initialized!");
        Preconditions.checkArgument(side != Side.BOTH, "Invalid Side: " + side.name());
        Preconditions.checkNotNull(gameDir, "gameDir cannot be null");
        Blueberry.side = side;
        Blueberry.gameDir = gameDir;
        try {
            modLoader = new BlueberryModLoader();
            BlueberryVersion version = getVersion();
            LOGGER.info("Loading " + name + " version " + version.getFullyQualifiedVersion() + " (" + getSide().getName() + ")");
            InternalBlueberryMod.register();
            Blueberry.getModLoader().loadMods();
            LOGGER.info("Loaded " + Blueberry.getModLoader().getLoadedMods().size() + " mods");
        } catch (Throwable throwable) {
            crash(throwable, "Initializing Blueberry");
        }
    }

    public static void crash(@NotNull("crashReport") CrashReport crashReport) {
        getUtil().crash(crashReport);
    }

    public static void crash(@NotNull Throwable throwable, @NotNull String message) {
        crash(CrashReport.forThrowable(throwable, message));
    }

    @Contract(value = " -> fail", pure = true)
    private Blueberry() {
        throw new IllegalStateException();
    }
}
