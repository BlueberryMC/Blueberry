package net.blueberrymc.common.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.blueberrymc.util.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class VersionChecker {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final long HALF_HOUR = 1000 * 60 * 30;
    @NotNull
    private static Result cachedResult = Result.ERROR;
    private static long lastCachedResult = 0;

    public static boolean isCached() {
        return System.currentTimeMillis() - lastCachedResult < HALF_HOUR;
    }

    @NotNull
    private static Supplier<Result> getOrRecord(boolean forceUseCachedData, @NotNull Supplier<Result> valueSupplier) {
        return () -> {
            if (forceUseCachedData) return Objects.requireNonNullElse(cachedResult, Result.ERROR);
            if (System.currentTimeMillis() - lastCachedResult > HALF_HOUR) {
                cachedResult = Objects.requireNonNullElse(valueSupplier.get(), Result.ERROR);
                lastCachedResult = System.currentTimeMillis();
            }
            return cachedResult;
        };
    }

    @NotNull
    public static CompletableFuture<@NotNull Result> check() {
        return check(false);
    }

    @NotNull
    public static CompletableFuture<@NotNull Result> check(boolean forceUseCachedData) {
        return CompletableFuture.supplyAsync(getOrRecord(forceUseCachedData, () -> {
            try {
                String url = "https://api.github.com/repos/" + Constants.GITHUB_REPO + "/compare/" + Versioning.getVersion().getBranch() + "..." + Versioning.getVersion().getCommit();
                LOGGER.info("Opening connection to {}", url);
                URLConnection connection = new URL(url).openConnection();
                if (!(connection instanceof HttpURLConnection conn)) {
                    throw new AssertionError("URLConnection is not instance of HttpURLConnection: " + connection.getClass().getTypeName());
                }
                connection.setDoInput(true);
                connection.connect();
                if (conn.getResponseCode() != 200) {
                    throw new IllegalStateException("GitHub API returned non-OK response code: " + conn.getResponseCode());
                }
                JsonObject obj = new Gson().fromJson(String.join("", IOUtils.readLines(conn.getInputStream(), StandardCharsets.UTF_8)), JsonObject.class);
                if (!obj.has("ahead_by") || !obj.has("behind_by")) {
                    throw new IllegalStateException("GitHub API returned invalid response");
                }
                return new Result(obj.get("ahead_by").getAsInt(), obj.get("behind_by").getAsInt());
            } catch (Exception | AssertionError e) {
                LOGGER.warn("Could not check for new version", e);
            }
            return Result.ERROR;
        }));
    }

    public record Result(int ahead, int behind) {
        private static final Result ERROR = new Result(-1, -1);

        @NotNull
        public String getStatusText() {
            if (ahead < 0 || behind < 0) return "Error checking for new version";
            if (ahead > 0 && behind > 0) return ahead + " commits ahead, " + behind + " commits behind";
            if (ahead > 0) return ahead + " commits ahead";
            if (behind > 0) return behind + " commits behind";
            return "up to date";
        }

        @NotNull
        public String getStatusKey() {
            if (ahead < 0 || behind < 0) return "error";
            if (ahead > 0 && behind > 0) return "diverged";
            if (ahead > 0) return "ahead";
            if (behind > 0) return "behind";
            return "clean";
        }
    }
}
