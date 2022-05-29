package net.blueberrymc.common.bml;

import net.blueberrymc.common.DeprecatedReason;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.security.CodeSigner;

public record ModFileEntry(@NotNull InputStream inputStream, @Nullable CodeSigner[] codeSigners) {
    /**
     * @deprecated Use {@link #inputStream()} instead.
     */
    @Deprecated
    @DeprecatedReason("Use #inputStream() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @NotNull
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * @deprecated Use {@link #codeSigners()} instead.
     */
    @Deprecated
    @DeprecatedReason("Use #codeSigners() instead")
    @ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
    @Nullable
    public CodeSigner[] getCodeSigners() {
        return codeSigners;
    }
}
