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
    public static final BlueberryVersion UNKNOWN = new BlueberryVersion("blueberry", "1.0.0", "unknown", "unknown", "9999-12-31T23:59:59+00:00", 0);
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
        int buildNumber = 0;
        if (stream != null) {
            try {
                properties.load(stream);
                name = properties.getProperty("name", name);
                version = properties.getProperty("version", version);
                magmaCubeCommit = properties.getProperty("magmaCubeCommit", magmaCubeCommit);
                commit = properties.getProperty("commit", commit);
                builtAt = properties.getProperty("builtAt", builtAt);
                try {
                    buildNumber = Integer.parseInt(properties.getProperty("buildNumber", "0"));
                } catch (NumberFormatException e) {
                    LOGGER.error("buildNumber is not a number: {}", properties.getProperty("buildNumber", "0"), e);
                }
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
        VERSION = new BlueberryVersion(name, version, magmaCubeCommit, commit, builtAt, buildNumber);
        return VERSION;
    }
}
