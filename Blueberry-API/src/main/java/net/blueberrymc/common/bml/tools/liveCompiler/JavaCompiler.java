package net.blueberrymc.common.bml.tools.liveCompiler;

import com.sun.tools.javac.Main;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.util.ClasspathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class JavaCompiler {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Compiles a single .java file.
     * @param file .java file to compile.
     * @return .class file
     */
    @NotNull
    public static File compile(@NotNull File file, @Nullable File dest) {
        if (!file.getName().endsWith(".java")) throw new IllegalArgumentException("Illegal file name (must ends with .java): " + file.getAbsolutePath());
        List<String> args = new ArrayList<>();
        args.add("-cp");
        args.add(ClasspathUtil.getClasspath(Blueberry.class));
        if (dest != null) {
            args.add("-d");
            args.add(dest.getAbsolutePath());
        }
        args.add(file.getAbsolutePath());
        Main.compile(args.toArray(new String[0]), new PrintWriter(System.err, true));
        return new File(file.getAbsolutePath().replaceAll("(.*)\\.java", "$1.class"));
    }

    /**
     * Compiles all .java files in specified directory, or just compiles a single .java file.
     * @param file the file(s) to compile
     */
    @NotNull
    public static File compileAll(@NotNull File file) throws IOException {
        if (!file.isDirectory() && !file.getName().endsWith(".java")) throw new IllegalArgumentException("Illegal file name (not a directory nor .java file: " + file.getAbsolutePath());
        Path path = file.toPath();
        File tmp = Files.createTempDirectory("blueberry-live-compiler-").toFile();
        tmp.deleteOnExit();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        Files.walk(file.toPath())
                .map(Path::toFile)
                .forEach(f -> {
                    if (throwable.get() != null) return;
                    if (f.isDirectory()) {
                        // if dir
                        File target = new File(tmp, path.relativize(f.toPath()).toString());
                        if (!target.mkdirs()) {
                            LOGGER.warn("Failed to create directory {} -> {}", f.getAbsolutePath(), target.getAbsolutePath());
                        } else {
                            LOGGER.debug("Created directory {} -> {}", f.getAbsolutePath(), target.getAbsolutePath());
                        }
                    } else {
                        // if file
                        if (f.getName().endsWith(".java")) {
                            compile(f, tmp);
                            if (!new File(tmp, path.relativize(f.toPath()).toString()).exists()) {
                                throwable.set(new RuntimeException("Compilation failed: " + f.getAbsolutePath()));
                                return;
                            }
                            LOGGER.debug("Compiled {} -> {}", f.getAbsolutePath(), tmp.getAbsolutePath());
                        } else {
                            File dest = new File(tmp, path.relativize(f.toPath()).toString());
                            try {
                                Files.copy(f.toPath(), dest.toPath());
                                LOGGER.debug("Copied {} -> {}", f.getAbsolutePath(), dest.getAbsolutePath());
                            } catch (IOException ex) {
                                LOGGER.warn("Failed to copy {} -> {}", f.getAbsolutePath(), dest.getAbsolutePath(), ex);
                            }
                        }
                    }
                });
        if (throwable.get() != null) {
            if (throwable.get() instanceof RuntimeException) throw (RuntimeException) throwable.get();
            throw new RuntimeException(throwable.get());
        }
        return tmp;
    }
}
