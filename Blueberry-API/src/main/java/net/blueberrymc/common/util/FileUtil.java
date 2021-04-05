package net.blueberrymc.common.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileUtil {
    public static void delete(@NotNull File file) throws IOException {
        if (!file.exists()) return;
        //noinspection ResultOfMethodCallIgnored
        Files.walk(file.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void copy(@NotNull File file, @NotNull File dest) throws IOException {
        Path path = file.toPath();
        Files.walk(file.toPath())
                .map(Path::toFile)
                .forEach(f -> {
                    if (f.isDirectory()) {
                        // if dir
                        File target = new File(dest, path.relativize(f.toPath()).toString());
                        target.mkdirs();
                    } else {
                        // if file
                        File target = new File(dest, path.relativize(f.toPath()).toString());
                        try {
                            Files.copy(f.toPath(), target.toPath());
                        } catch (IOException ignored) {
                        }
                    }
                });
    }
}
