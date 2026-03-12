package net.blueberrymc.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.blueberrymc.common.DeprecatedReason;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;

public class DetectedVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String id;
    private final String name;
    private final boolean stable;
    private final DataVersion worldVersion;
    private final int protocolVersion;
    private final Date buildTime;

    private DetectedVersion(JsonObject json) {
        this.id = json.get("id").getAsString();
        this.name = json.get("name").getAsString();
        this.stable = json.get("stable").getAsBoolean();
        this.worldVersion = new DataVersion(json.get("world_version").getAsInt(), json.has("series_id") ? json.get("series_id").getAsString() : "main");
        this.protocolVersion = json.get("protocol_version").getAsInt();
        this.buildTime = Date.from(ZonedDateTime.parse(json.get("build_time").getAsString()).toInstant());
    }

    @Nullable
    public static DetectedVersion tryDetectVersion() {
        try (InputStream in = DetectedVersion.class.getResourceAsStream("/version.json")) {
            if (in == null) {
                LOGGER.warn("Missing version information!");
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(in)) {
                return new DetectedVersion(new Gson().fromJson(reader, JsonObject.class));
            }
        } catch (JsonParseException | IOException var8) {
            throw new IllegalStateException("Game version information is corrupt", var8);
        }
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public DataVersion getDataVersion() {
        return this.worldVersion;
    }

    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    @Deprecated
    @DeprecatedReason("Use SharedConstants instead")
    public int getPackVersion(@NotNull PackType packType) {
        return 0;
    }

    @NotNull
    public Date getBuildTime() {
        return this.buildTime;
    }

    public boolean isStable() {
        return this.stable;
    }
}
