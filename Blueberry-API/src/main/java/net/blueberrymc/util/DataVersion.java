package net.blueberrymc.util;

import org.jetbrains.annotations.NotNull;

public record DataVersion(int version, @NotNull String series) {
}
