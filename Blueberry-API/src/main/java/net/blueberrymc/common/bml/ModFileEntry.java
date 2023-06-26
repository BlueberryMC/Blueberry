package net.blueberrymc.common.bml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.security.CodeSigner;

public record ModFileEntry(@NotNull InputStream inputStream, @Nullable CodeSigner[] codeSigners) {
}
