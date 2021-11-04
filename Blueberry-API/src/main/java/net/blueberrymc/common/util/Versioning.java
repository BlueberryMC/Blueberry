package net.blueberrymc.common.util;

import net.blueberrymc.common.Blueberry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Versioning {
    private static final Logger LOGGER = LogManager.getLogger();
    // cached version info
    private static BlueberryVersion VERSION = null;

    /**
     * Fetches the blueberry version file.
     * @return version info
     */
    @NotNull
    public static BlueberryVersion getVersion() {
        if (VERSION != null) return VERSION;
        InputStream stream = ResourceLocator.getInstance(Blueberry.class).getResourceAsStream("/api-version.properties");
        Properties properties = new Properties();
        String name = "blueberry";
        String version = "unknown";
        String magmaCubeCommit = "unknown";
        String commit = "unknown";
        String builtAt = "unknown";
        if (stream != null) {
            try {
                properties.load(stream);
                name = properties.getProperty("name", name);
                version = properties.getProperty("version", version);
                magmaCubeCommit = properties.getProperty("magmaCubeCommit", magmaCubeCommit);
                commit = properties.getProperty("commit", commit);
                builtAt = properties.getProperty("builtAt", builtAt);
            } catch (IOException ex) {
                LOGGER.error("Blueberry API version information is corrupt (api-version.properties)", ex);
            }
        } else {
            stream = ResourceLocator.getInstance(Blueberry.class).getResourceAsStream("/maven/net.blueberrymc/blueberry-api/pom.properties");
            if (stream != null) {
                try {
                    properties.load(stream);
                    version = properties.getProperty("version", version);
                } catch (IOException ex) {
                    LOGGER.error("Blueberry API version information is corrupt (pom.properties)", ex);
                }
            }
        }
        VERSION = new BlueberryVersion(name, version, magmaCubeCommit, commit, builtAt);
        return VERSION;
    }
}
