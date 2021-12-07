package net.blueberrymc.common.launch;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.util.UniversalClassLoader;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.server.main.ServerMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BlueberryPreBootstrap {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean bootstrapped = false;
    private static UniversalClassLoader universalClassLoader = null;

    public static void preBootstrap(@NotNull Side side, @NotNull File gameDir) {
        Preconditions.checkArgument(!bootstrapped, "Blueberry is already pre-bootstrapped!");
        Preconditions.checkArgument(side != Side.BOTH, "Invalid Side: " + side.name());
        Preconditions.checkNotNull(gameDir, "gameDir cannot be null");
        File modsDir = new File(gameDir, "mods");
        if (!modsDir.exists() && !modsDir.mkdir()) {
            LOGGER.warn("Could not create mods directory");
        }
        if (modsDir.isFile()) {
            throw new IllegalStateException("mods directory is not a directory");
        }
        init(modsDir);
        bootstrapped = true;
    }

    public static void init(@NotNull File modsDir) {
        for (File file : lookForMods(modsDir)) {
            try {
                addToUniversalClassLoader(file.toURI().toURL());
            } catch (Throwable e) {
                LOGGER.warn("Could not add into the classpath: {}", file.getAbsolutePath(), e);
            }
        }
    }

    public static void destroyUniversalClassLoader() {
        if (universalClassLoader != null) {
            try {
                universalClassLoader.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to destroy UniversalClassLoader", e);
            }
        }
        universalClassLoader = null;
    }

    public static void addURLsToSet(@NotNull Set<URL> urlSet) {
        if (universalClassLoader != null) {
            urlSet.addAll(Arrays.asList(universalClassLoader.getURLs()));
        }
    }
    
    @NotNull
    public static Deque<File> lookForMods(@NotNull File modsDir) {
        LOGGER.info("Looking for mods in " + modsDir.getAbsolutePath());
        Deque<File> toLoad = new ConcurrentLinkedDeque<>();
        if (ServerMain.tempModDir != null) toLoad.add(ServerMain.tempModDir);
        int dirCount = 0;
        int fileCount = 0;
        File[] files = modsDir.listFiles();
        for (File file : Objects.requireNonNull(files)) {
            if (file.isDirectory()) {
                if (file.getName().equals(Versioning.getVersion().getGameVersion())) {
                    for (File f : Objects.requireNonNull(file.listFiles())) {
                        if (f.isDirectory()) {
                            File descriptionFile = new File(f, "mod.yml");
                            if (descriptionFile.exists()) {
                                if (descriptionFile.isDirectory()) {
                                    LOGGER.error(descriptionFile.getAbsolutePath() + " exists but is not a file");
                                    continue;
                                }
                            }
                            dirCount++;
                            toLoad.add(f);
                        } else {
                            if (f.getName().equals(".zip") || f.getName().equals(".jar")) {
                                fileCount++;
                                toLoad.add(f);
                            }
                        }
                    }
                }
                File descriptionFile = new File(file, "mod.yml");
                if (descriptionFile.exists()) {
                    if (descriptionFile.isDirectory()) {
                        LOGGER.error(descriptionFile.getAbsolutePath() + " exists but is not a file");
                        continue;
                    }
                }
                dirCount++;
                toLoad.add(file);
            } else {
                if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
                    fileCount++;
                    toLoad.add(file);
                }
            }
        }
        LOGGER.info("Found {} files to load (files: {}, directories: {})", toLoad.size(), fileCount, dirCount);
        return toLoad;
    }

    private static void addToUniversalClassLoader(@NotNull URL url) {
        if (universalClassLoader == null) {
            universalClassLoader = new UniversalClassLoader(new URL[]{url});
        } else {
            universalClassLoader.addURL(url);
        }
    }
}
