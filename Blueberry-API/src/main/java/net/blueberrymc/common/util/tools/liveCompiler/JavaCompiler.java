package net.blueberrymc.common.util.tools.liveCompiler;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonDeserializer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.types.Type;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.blueberrymc.client.EarlyLoadingMessageManager;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.util.ClasspathUtil;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.LoggedPrintStream;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.ClassVisitor;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
import java.util.concurrent.atomic.AtomicReference;

public class JavaCompiler {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Set<String> classpath;

    static {
        Set<String> cp = new HashSet<>();
        cp.add(ClasspathUtil.getClasspath(Blueberry.class)); // Blueberry-API
        cp.add(ClasspathUtil.getClasspath(MinecraftServer.class)); // Minecraft
        cp.add(ClasspathUtil.getClasspath(Nonnull.class)); // javax
        cp.add(ClasspathUtil.getClasspath(Launch.class)); // Launch Wrapper
        cp.add(ClasspathUtil.getClasspath(GLFW.class)); // OpenGL
        cp.add(ClasspathUtil.getClasspath(ClassVisitor.class));// ASM
        cp.add(ClasspathUtil.getClasspath(Mixin.class)); // Mixin
        cp.add(ClasspathUtil.getClasspath(PoseStack.class)); // Blaze3d
        cp.add(ClasspathUtil.getClasspath(ImmutableMap.class)); // Guava
        cp.add(ClasspathUtil.getClasspath(Float2FloatOpenHashMap.class)); // fastutil
        cp.add(ClasspathUtil.getClasspath(StringUtils.class)); // commons-lang3
        cp.add(ClasspathUtil.getClasspath(JsonDeserializer.class)); // Gson
        cp.add(ClasspathUtil.getClasspath(Type.class)); // DataFixerUpper
        cp.add(ClasspathUtil.getClasspath(Message.class)); // Brigadier
        cp.add(ClasspathUtil.getClasspath(GameVersion.class)); // javabridge
        try {
            // these class are not in classpath of Blueberry-API, so we need to do this
            cp.add(ClasspathUtil.getClasspath(Class.forName("net.minecraft.client.gui.ScreenManager"))); // MinecraftForge-API
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        classpath = ImmutableSet.copyOf(cp);
        LOGGER.info("Classpath for compiler: " + Joiner.on(";").join(classpath));
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
            args.add(Joiner.on(";").join(classpath) + ";" + root.getAbsolutePath());
        }
        if (dest != null) {
            args.add("-d");
            args.add(dest.getAbsolutePath());
        }
        args.add("-proc:none");
        args.add("-source");
        args.add("17");
        args.add(file.getAbsolutePath());
        PrintStream ps = new PrintStream(new WriterOutputStream(new PrintWriter(new LoggedPrintStream("Blueberry Live Compiler", System.err), true), StandardCharsets.UTF_8));
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new RuntimeException("JavaCompiler is not available");
        int result = compiler.run(System.in, ps, ps, args.toArray(new String[0]));
        if (result != 0) LOGGER.warn("Compiler (for file {}) exited with code: {}", file.getAbsolutePath(), result);
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
        int nThreads = Math.max(4, Runtime.getRuntime().availableProcessors());
        ExecutorService compilerExecutor = Executors.newFixedThreadPool(nThreads, new ThreadFactoryBuilder().setNameFormat("Blueberry Mod Compiler Worker #%d").build());
        LOGGER.info("Compiling the source code using up to " + nThreads + " threads");
        EarlyLoadingMessageManager.logModCompiler("Compiling the source code using up to " + nThreads + " threads");
        AtomicBoolean first = new AtomicBoolean(true);
        Files.walk(file.toPath())
                .map(Path::toFile)
                .forEach(f -> {
                    if (throwable.get() != null) return;
                    if (f.isDirectory()) {
                        // if dir
                        File target = new File(tmp, path.relativize(f.toPath()).toString());
                        if (!target.mkdirs() && !target.getAbsolutePath().equals(tmp.getAbsolutePath())) {
                            LOGGER.warn("Failed to create directory {} -> {}", f.getAbsolutePath(), target.getAbsolutePath());
                        } else {
                            LOGGER.debug("Created directory {} -> {}", f.getAbsolutePath(), target.getAbsolutePath());
                        }
                    } else {
                        // if file
                        if (f.getName().endsWith(".java")) {
                            Runnable doCompile = () -> {
                                String rel = path.relativize(f.toPath()).toString().replaceAll("(.*)\\.java", "$1.class");
                                LOGGER.info("Compiling: " + rel);
                                EarlyLoadingMessageManager.logModCompiler("Compiling: " + rel);
                                compile(file, f, tmp);
                                if (!new File(tmp, rel).exists()) {
                                    throwable.set(new RuntimeException("Compilation failed: " + rel));
                                    EarlyLoadingMessageManager.logModCompiler("Failed to compile: " + rel);
                                    LOGGER.error("Failed to compile: " + rel);
                                    return;
                                }
                                LOGGER.debug("Compiled {} -> {}", f.getAbsolutePath(), tmp.getAbsolutePath());
                                EarlyLoadingMessageManager.logModCompiler("Compiled: " + rel);
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
                                        EarlyLoadingMessageManager.logModCompiler("Failed to compile: " + rel);
                                        LOGGER.error("Failed to compile: " + rel);
                                    }
                                });
                            }
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
        compilerExecutor.shutdown();
        try {
            if (!compilerExecutor.awaitTermination(5L, TimeUnit.MINUTES)) {
                LOGGER.warn("Timed out compilation. Some files may be missing.");
                EarlyLoadingMessageManager.logModCompiler("Timed out compilation. Some files may be missing.");
            }
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
