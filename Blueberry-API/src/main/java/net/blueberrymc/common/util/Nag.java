package net.blueberrymc.common.util;

import net.blueberrymc.common.DeprecatedReason;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.loading.ModLoadingError;
import net.blueberrymc.common.bml.loading.ModLoadingErrors;
import net.blueberrymc.config.ModDescriptionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;

public class Nag extends RuntimeException {
    private static final Logger LOGGER = LogManager.getLogger();

    private Nag(@NotNull String message) {
        super(message);
    }

    private static void nag(@Nullable BlueberryMod mod, @NotNull String message) {
        if (mod == null) {
            String msg = generatePrefix(null) + message;
            LOGGER.warn("", new Nag(msg));
            ModLoadingErrors.add(new ModLoadingError(null, msg, true));
        } else {
            String msg = generatePrefix(mod.getDescription()) + message;
            mod.getLogger().warn("", new Nag(msg));
            ModLoadingErrors.add(new ModLoadingError(mod, msg, true));
        }
    }

    @NotNull
    private static String generatePrefix(@Nullable ModDescriptionFile mdf) {
        String author;
        if (mdf != null && mdf.getAuthors() != null) {
            if (mdf.getAuthors().size() == 1) {
                author = "author";
            } else {
                author = "authors";
            }
        } else {
            author = "author";
        }
        // <unknown> if mdf or authors is null
        String joinedAuthor = mdf == null || mdf.getAuthors() == null
                ? "<unknown>" :
                String.join(", ", mdf.getAuthors());
        // <unknown> if mdf is null
        String modId = mdf == null ? "<unknown>" : mdf.getModId();
        return "Please notify the mod " + author + " of " + modId + " (" + joinedAuthor + ") about following: ";
    }

    public static void deprecatedEvent(@NotNull Class<?> deprecatedEvent, @NotNull BlueberryMod mod) {
        DeprecatedData data = DeprecatedData.of(deprecatedEvent);
        deprecated(mod, "Event " + deprecatedEvent.getTypeName(), data);
    }

    public static void deprecated(@Nullable BlueberryMod mod, @NotNull String what, @NotNull DeprecatedData data) {
        if (data.empty()) {
            return;
        }
        String message = what + " is deprecated";
        if (data.since() != null) {
            message += " since " + data.since();
        }
        if (data.reason() != null) {
            message += " because \"" + data.reason() + "\"";
        }
        if (data.forRemoval()) {
            String when = "a future version";
            if (data.scheduledRemoval() != null) {
                when = data.scheduledRemoval();
            }
            message += " and will be removed in " + when;
        }
        nag(mod, message);
    }

    private record DeprecatedData(boolean empty, @Nullable String since, boolean forRemoval, @Nullable String reason, @Nullable String scheduledRemoval) {
        @Contract("_ -> new")
        @NotNull
        private static DeprecatedData of(@NotNull AnnotatedElement element) {
            boolean empty = true;
            ApiStatus.ScheduledForRemoval scheduledForRemoval = element.getAnnotation(ApiStatus.ScheduledForRemoval.class);
            DeprecatedReason deprecatedReason = element.getAnnotation(DeprecatedReason.class);
            Deprecated deprecated = element.getAnnotation(Deprecated.class);
            if (deprecated == null) {
                if (deprecatedReason != null) {
                    nag(BlueberryMod.detectModFromElement(element), "Annotated with @DeprecatedReason but not annotated with @Deprecated");
                }
                if (scheduledForRemoval != null) {
                    nag(BlueberryMod.detectModFromElement(element), "Annotated with @ApiStatus.ScheduledForRemoval but not annotated with @Deprecated");
                }
            }
            String since = null;
            boolean forRemoval = false;
            String reason = null;
            String scheduledRemoval = null;
            if (deprecated != null) {
                empty = false;
                since = deprecated.since();
                forRemoval = deprecated.forRemoval();
                if (since.isEmpty()) since  = null;
            }
            if (deprecatedReason != null) {
                empty = false;
                reason = deprecatedReason.value();
                if (reason.isEmpty()) reason = null;
            }
            if (scheduledForRemoval != null) {
                empty = false;
                forRemoval = true;
                scheduledRemoval = scheduledForRemoval.inVersion();
                if (scheduledRemoval.isEmpty()) scheduledRemoval = null;
            }
            return new DeprecatedData(empty, since, forRemoval, reason, scheduledRemoval);
        }
    }
}
