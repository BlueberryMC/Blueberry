package net.blueberrymc.common.util;

import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.loading.ModLoadingError;
import net.blueberrymc.common.bml.loading.ModLoadingErrors;
import net.blueberrymc.config.ModDescriptionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Nag extends RuntimeException {
    private static final Logger LOGGER = LogManager.getLogger();

    private Nag(@NotNull String message) {
        super(message);
    }

    public static void nag(@Nullable BlueberryMod mod, @NotNull String message) {
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
        String modId = mdf == null ? "<unknown>" : mdf.modId();
        return "Please notify the mod " + author + " of " + modId + " (" + joinedAuthor + ") about following: ";
    }

    public static void deprecatedEvent(@NotNull Class<?> deprecatedEvent, @NotNull BlueberryMod mod) {
        DeprecatedData data = DeprecatedData.of(deprecatedEvent);
        deprecated(mod, "Event " + deprecatedEvent.getTypeName(), data);
    }

    public static void deprecated(@Nullable BlueberryMod mod, @NotNull String what, @NotNull DeprecatedData data) {
        if (!data.deprecated()) {
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
}
