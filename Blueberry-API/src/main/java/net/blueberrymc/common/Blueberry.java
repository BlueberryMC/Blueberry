package net.blueberrymc.common;

import com.google.common.base.Preconditions;
import net.blueberrymc.client.BlueberryClient;
import net.blueberrymc.common.bml.BlueberryModLoader;
import net.blueberrymc.common.bml.EventManager;
import net.blueberrymc.common.bml.ModLoader;
import net.blueberrymc.common.bml.ModManager;
import net.blueberrymc.common.util.BlueberryVersion;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.server.BlueberryServer;
import net.minecraft.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Blueberry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String name = "Blueberry";
    private static final BlueberryModLoader modLoader = new BlueberryModLoader();
    private static final EventManager eventManager = new EventManager();
    private static final ModManager modManager = new ModManager();
    private static Side side;
    private static BlueberryUtil util;

    @NotNull
    public static ModLoader getModLoader() {
        return modLoader;
    }

    @NotNull
    public static BlueberryVersion getVersion() {
        return Versioning.getVersion();
    }

    @NotNull
    public static EventManager getEventManager() {
        return eventManager;
    }

    @NotNull
    public static ModManager getModManager() {
        return modManager;
    }

    public static Side getSide() {
        return side;
    }

    public static boolean isClient() {
        return side == Side.CLIENT;
    }

    public static boolean isServer() {
        return side == Side.SERVER;
    }

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

    public static void bootstrap(@NotNull Side side) {
        Preconditions.checkArgument(side != Side.BOTH, "Invalid Side: " + side.name());
        Blueberry.side = side;
        try {
            BlueberryVersion version = getVersion();
            LOGGER.info("Loading " + name + " version " + version.getFullyQualifiedVersion() + " (" + getSide().getName() + ")");
            InternalMagmaCubeMod.register();
            InternalBlueberryMod.register();
            Blueberry.getModLoader().loadMods();
            LOGGER.info("Loaded " + Blueberry.getModLoader().getLoadedMods().size() + " mods");
        } catch (Throwable throwable) {
            crash(throwable, "Initializing Blueberry");
        }
    }

    public static void crash(CrashReport crashReport) {
        if (isClient()) {
            net.minecraft.client.Minecraft.fillReport(null, null, null, crashReport);
            net.minecraft.client.Minecraft.crash(crashReport);
        } else {
            File file = new File(new File("crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
            LOGGER.error(crashReport.getFriendlyReport());
            if (crashReport.saveToFile(file)) {
                LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }
            if (isServer()) ((BlueberryServer) util).stopServer();
            System.exit(1);
        }
    }

    public static void crash(Throwable throwable, String message) {
        crash(CrashReport.forThrowable(throwable, message));
    }
}
