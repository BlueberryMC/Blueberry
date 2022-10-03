package net.blueberrymc.common.util;

import net.blueberrymc.common.DeprecatedReason;
import net.blueberrymc.common.bml.BlueberryMod;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;

public record DeprecatedData(boolean deprecated, @Nullable String since, boolean forRemoval, @Nullable String reason, @Nullable String scheduledRemoval) {
    public static final DeprecatedData NOT_DEPRECATED = new DeprecatedData(false, null, false, null, null);

    @Contract("_ -> new")
    @NotNull
    public static DeprecatedData of(@NotNull AnnotatedElement element) {
        boolean actuallyDeprecated = false;
        ApiStatus.ScheduledForRemoval scheduledForRemoval = element.getAnnotation(ApiStatus.ScheduledForRemoval.class);
        DeprecatedReason deprecatedReason = element.getAnnotation(DeprecatedReason.class);
        Deprecated deprecated = element.getAnnotation(Deprecated.class);
        if (deprecated == null) {
            if (deprecatedReason != null) {
                Nag.nag(BlueberryMod.detectModFromElement(element), "Annotated with @DeprecatedReason but not annotated with @Deprecated");
            }
            if (scheduledForRemoval != null) {
                Nag.nag(BlueberryMod.detectModFromElement(element), "Annotated with @ApiStatus.ScheduledForRemoval but not annotated with @Deprecated");
            }
        }
        if (scheduledForRemoval != null && deprecated != null && !deprecated.forRemoval()) {
            Nag.nag(BlueberryMod.detectModFromElement(element), "Annotated with @ApiStatus.ScheduledForRemoval but is not marked for removal with @Deprecated(forRemoval = true)");
        }
        String since = null;
        boolean forRemoval = false;
        String reason = null;
        String scheduledRemoval = null;
        if (deprecated != null) {
            actuallyDeprecated = true;
            since = deprecated.since();
            forRemoval = deprecated.forRemoval();
            if (since.isEmpty()) since = null;
        }
        if (deprecatedReason != null) {
            actuallyDeprecated = true;
            reason = deprecatedReason.value();
            if (reason.isEmpty()) reason = null;
        }
        if (scheduledForRemoval != null) {
            actuallyDeprecated = true;
            forRemoval = true;
            scheduledRemoval = scheduledForRemoval.inVersion();
            if (scheduledRemoval.isEmpty()) scheduledRemoval = null;
        }
        return new DeprecatedData(actuallyDeprecated, since, forRemoval, reason, scheduledRemoval);
    }
}
