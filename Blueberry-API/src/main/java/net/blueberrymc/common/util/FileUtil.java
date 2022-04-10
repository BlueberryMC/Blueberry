package net.blueberrymc.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;

/**
 * Some utility methods for dealing with files.
 */
public class FileUtil {
    /**
     * Checks if the file represents the root directory.
     * @param file the file to check
     * @return true if the file is the root directory; false otherwise
     */
    public static boolean isRoot(@NotNull File file) {
        try {
            return file.getCanonicalFile().toPath().getNameCount() == 0;
        } catch (IOException ignore) {
            return false;
        }
    }

    /**
     * Checks if the current environment has the multiple roots (e.g. multiple drives in Windows).
     * @return true if there are multiple roots; false otherwise (1 or 0)
     */
    public static boolean hasMultipleRoots() {
        return File.listRoots().length > 1;
    }

    /**
     * Checks if the given file or directory is in a boundary directory.
     * @param boundary The boundary directory.
     * @param file The file to check.
     * @return true if the file is in the boundary directory or boundary directory is null; false otherwise.
     */
    @Contract("null, _ -> true")
    public static boolean isFileInsideBoundary(@Nullable File boundary, @NotNull File file) {
        if (boundary == null) return true;
        try {
            return boundary.equals(file) || file.getCanonicalPath().startsWith(boundary.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Deletes the given file or directory recursively.
     * @param file The file or directory to delete.
     * @throws IOException If an I/O error occurs.
     */
    public static void delete(@NotNull File file) throws IOException {
        if (!file.exists()) return;
        if (file.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        Files.walk(file.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * Gets the location of the .minecraft directory.
     * @return the .minecraft directory
     */
    @NotNull
    public static File getMinecraftDir() {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String mcDir = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null) {
            return new File(System.getenv("APPDATA"), mcDir);
        } else if (osType.contains("mac")) {
            return new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft");
        }
        return new File(userHomeDir, mcDir);
    }

    /**
     * Copies the given file or directory to the given destination (recursively).
     * @param file The file or directory to copy.
     * @param dest The destination.
     * @throws IOException If an I/O error occurs.
     */
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
