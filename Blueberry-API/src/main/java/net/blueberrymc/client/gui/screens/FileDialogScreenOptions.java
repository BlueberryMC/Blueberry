package net.blueberrymc.client.gui.screens;

import net.blueberrymc.common.util.FileUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

public record FileDialogScreenOptions(
        @Nullable File boundary,
        @NotNull Component title,
        @NotNull FileType fileType,
        @Nullable Callback callback,
        @NotNull Predicate<File> filter,
        @Nullable File initialDirectory
) {
    public void runCallback(@Nullable File file) {
        if (callback != null) {
            callback.onFileSelected(file);
        }
    }

    @NotNull
    public File getInitialDirectory() {
        try {
            if (initialDirectory != null) return initialDirectory.getCanonicalFile();
        } catch (IOException ignore) {}
        try {
            if (boundary != null) return boundary.getCanonicalFile();
        } catch (IOException ignore) {}
        return new File(".");
    }

    @Contract(value = "-> new", pure = true)
    public @NotNull Builder toBuilder() {
        return builder()
                .boundary(boundary)
                .title(title)
                .fileType(fileType)
                .callback(callback)
                .filter(filter)
                .initialDirectory(initialDirectory);
    }

    @Contract(value = "-> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    enum FileType {
        FILE,
        DIRECTORY,
        ALL
    }

    @FunctionalInterface
    public interface Callback {
        /**
         * Called when the user selects a file. Returns null if the user cancelled the dialog.
         * @param file the file
         */
        void onFileSelected(@Nullable File file);
    }

    // Builder with private constructor
    public static final class Builder {
        @Nullable private File boundary;
        @NotNull private Component title = Component.literal("Select a file");
        @NotNull private FileType fileType = FileType.ALL;
        @Nullable private Callback callback;
        @NotNull private Predicate<File> filter = (file) -> true;
        @Nullable private File initialDirectory;

        private Builder() {}

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder boundary(@Nullable File boundary) {
            this.boundary = boundary;
            checkBoundaryAndInitialDirectory();
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder title(@NotNull Component title) {
            Objects.requireNonNull(title, "title cannot be null");
            this.title = title;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder fileType(@NotNull FileType fileType) {
            Objects.requireNonNull(fileType, "fileType cannot be null");
            this.fileType = fileType;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder callback(@Nullable Callback callback) {
            this.callback = callback;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder filter(@NotNull Predicate<@NotNull File> filter) {
            Objects.requireNonNull(filter, "filter cannot be null");
            this.filter = filter;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public @NotNull Builder initialDirectory(@Nullable File initialDirectory) {
            this.initialDirectory = initialDirectory;
            checkBoundaryAndInitialDirectory();
            return this;
        }

        @Contract(" -> new")
        public @NotNull FileDialogScreenOptions build() {
            checkBoundaryAndInitialDirectory();
            return new FileDialogScreenOptions(boundary, title, fileType, callback, filter, initialDirectory);
        }

        private void checkBoundaryAndInitialDirectory() {
            if (boundary != null && !boundary.isDirectory()) {
                throw new IllegalArgumentException("boundary must be a directory");
            }
            if (initialDirectory != null) {
                if (!initialDirectory.isDirectory()) {
                    throw new RuntimeException("initialDirectory must be a directory");
                }
                if (!FileUtil.isFileInsideBoundary(boundary, initialDirectory)) {
                    throw new IllegalArgumentException("initialDirectory must be inside boundary");
                }
            }
        }
    }
}
