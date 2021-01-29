package net.blueberrymc.common.bml.loading;

import net.blueberrymc.common.bml.ModInfo;
import net.blueberrymc.common.util.LazyValue;
import org.jetbrains.annotations.NotNull;

public class ModLoadingError {
    public final ModInfo modInfo;
    public final Throwable throwable;
    public final boolean isWarning;
    private final LazyValue<String> message = new LazyValue<>(this::generateMessage);

    public ModLoadingError(@NotNull ModInfo modInfo, @NotNull String message, boolean isWarning) {
        this(modInfo, new RuntimeException(message), isWarning);
    }

    public ModLoadingError(@NotNull ModInfo modInfo, @NotNull Throwable throwable, boolean isWarning) {
        this.modInfo = modInfo;
        this.throwable = throwable;
        this.isWarning = isWarning;
    }

    private String generateMessage() {
        Throwable cause = throwable;
        String message = cause.getMessage();
        while ((cause = cause.getCause()) != null) {
            message = cause.getMessage();
        }
        return message;
    }

    public String getMessage() {
        return message.get();
    }
}
