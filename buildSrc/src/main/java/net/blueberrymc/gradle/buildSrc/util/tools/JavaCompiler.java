package net.blueberrymc.gradle.buildSrc.util.tools;

import io.sigpipe.jbsdiff.Patch;
import net.blueberrymc.gradle.buildSrc.util.ClasspathUtil;
import net.blueberrymc.gradle.buildSrc.util.ThreadLocalLoggedBufferedOutputStream;
import net.blueberrymc.nativeutil.NativeUtil;
import org.apache.commons.compress.compressors.CompressorException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class JavaCompiler {
    public static Logger logger;
    public static final Set<String> classpath;
    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    static {
        Set<String> cp = new HashSet<>();
        try {
            cp.add(ClasspathUtil.getClasspath(CompressorException.class)); // commons-compress
            cp.add(ClasspathUtil.getClasspath(Patch.class)); // jbsdiff
            cp.add(ClasspathUtil.getClasspath(NativeUtil.class)); // NativeUtil
        } catch (Exception | NoClassDefFoundError e) {
            e.printStackTrace();
        }
        classpath = cp;
    }

    /**
     * Compiles a single .java file.
     * @param file .java file to compile.
     * @return .class file
     */
    @NotNull
    public static File compile(@NotNull File root, @NotNull File file, @Nullable File dest) {
        if (!file.getName().endsWith(".java")) throw new IllegalArgumentException("Illegal file name (must ends with .java): " + file.getAbsolutePath());
        List<String> args = new ArrayList<>();
        if (!classpath.isEmpty()) {
            args.add("-cp");
            args.add(String.join(File.pathSeparator, classpath) + File.pathSeparator + root.getAbsolutePath());
        }
        if (dest != null) {
            args.add("-d");
            args.add(dest.getAbsolutePath());
        }
        args.add("-proc:none");
        args.add("-source");
        args.add("17");
        args.add(file.getAbsolutePath());
        OutputStream out = new ThreadLocalLoggedBufferedOutputStream(logger, "Blueberry Live Compiler");
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new RuntimeException("JavaCompiler is not available. Make sure you're running a JDK.");
        int result = compiler.run(System.in, out, out, args.toArray(new String[0]));
        if (result != 0) logger.warn("Compiler (for file {}) exited with code: {}", file.getAbsolutePath(), result);
        return new File(file.getAbsolutePath().replaceAll("(.*)\\.java", "$1.class"));
    }

    /**
     * Compiles all .java files in specified directory, or just compiles a single .java file.
     * @param file the file(s) to compile
     */
    @NotNull
    public static File compileAll(@NotNull File file) throws IOException {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("File is not a directory: " + file.getAbsolutePath());
        }
        Path path = file.toPath();
        File tmp = Files.createTempDirectory("blueberry-live-compiler-").toFile();
        tmp.deleteOnExit();
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        int nThreads = Math.max(4, Runtime.getRuntime().availableProcessors());
        ExecutorService compilerExecutor = Executors.newFixedThreadPool(nThreads, r -> new Thread(r, "Blueberry Mod Compiler Worker #" + THREAD_ID.getAndIncrement()));
        logger.info("Compiling the source code using up to " + nThreads + " threads");
        AtomicBoolean first = new AtomicBoolean(true);
        try (Stream<Path> walk = Files.walk(file.toPath())) {
            walk.map(Path::toFile).forEach(f -> {
                if (throwable.get() != null) return;
                if (f.isDirectory()) {
                    // if dir
                    File target = new File(tmp, path.relativize(f.toPath()).toString());
                    if (!target.mkdirs() && !target.getAbsolutePath().equals(tmp.getAbsolutePath())) {
                        logger.warn("Failed to create directory {} -> {}", f.getAbsolutePath(), target.getAbsolutePath());
                    } else {
                        logger.info("Created directory {} -> {}", f.getAbsolutePath(), target.getAbsolutePath());
                    }
                } else {
                    // if file
                    if (f.getName().endsWith(".java")) {
                        Runnable doCompile = () -> {
                            String rel = path.relativize(f.toPath()).toString().replaceAll("(.*)\\.java", "$1.class");
                            logger.info("Compiling: " + rel);
                            compile(file, f, tmp);
                            if (!new File(tmp, rel).exists()) {
                                throwable.set(new RuntimeException("Compilation failed: " + rel));
                                logger.error("Failed to compile: " + rel);
                                return;
                            }
                            logger.info("Compiled {} -> {}", f.getAbsolutePath(), tmp.getAbsolutePath());
                        };
                        if (first.get()) { // to prevent race condition
                            first.set(false);
                            doCompile.run();
                        } else {
                            compilerExecutor.submit(() -> {
                                if (throwable.get() != null) return;
                                try {
                                    doCompile.run();
                                } catch (Exception throwable1) {
                                    String rel = path.relativize(f.toPath()).toString().replaceAll("(.*)\\.java", "$1.class");
                                    throwable.set(new RuntimeException("Compilation failed: " + rel, throwable1));
                                    logger.error("Failed to compile: " + rel);
                                }
                            });
                        }
                    } else {
                        File dest = new File(tmp, path.relativize(f.toPath()).toString());
                        try {
                            Files.copy(f.toPath(), dest.toPath());
                            logger.debug("Copied {} -> {}", f.getAbsolutePath(), dest.getAbsolutePath());
                        } catch (IOException ex) {
                            logger.warn("Failed to copy {} -> {}", f.getAbsolutePath(), dest.getAbsolutePath(), ex);
                        }
                    }
                }
            });
        }
        compilerExecutor.shutdown();
        try {
            //noinspection ResultOfMethodCallIgnored
            compilerExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (throwable.get() != null) {
            if (throwable.get() instanceof RuntimeException) throw (RuntimeException) throwable.get();
            throw new RuntimeException(throwable.get());
        }
        return tmp;
    }
}
