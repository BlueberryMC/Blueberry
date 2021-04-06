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
import com.sun.tools.javac.Main;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.util.ClasspathUtil;
import net.blueberrymc.util.NoopPrintStream;
import net.minecraft.SharedConstants;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.LoggedPrintStream;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.ClassVisitor;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
        cp.add(ClasspathUtil.getClasspath(Blueberry.class));
        cp.add(ClasspathUtil.getClasspath(MinecraftServer.class));
        cp.add(ClasspathUtil.getClasspath(Nonnull.class));
        cp.add(ClasspathUtil.getClasspath(Launch.class));
        cp.add(ClasspathUtil.getClasspath(GLFW.class));
        cp.add(ClasspathUtil.getClasspath(ClassVisitor.class));
        cp.add(ClasspathUtil.getClasspath(Mixin.class));
        cp.add(ClasspathUtil.getClasspath(PoseStack.class));
        cp.add(ClasspathUtil.getClasspath(ImmutableMap.class));
        cp.add(ClasspathUtil.getClasspath(Float2FloatOpenHashMap.class));
        cp.add(ClasspathUtil.getClasspath(StringUtils.class));
        cp.add(ClasspathUtil.getClasspath(JsonDeserializer.class));
        cp.add(ClasspathUtil.getClasspath(Type.class));
        cp.add(ClasspathUtil.getClasspath(Message.class));
        cp.add(ClasspathUtil.getClasspath(GameVersion.class));
        try {
            cp.add(ClasspathUtil.getClasspath(Class.forName("net.minecraft.client.gui.ScreenManager"))); // this class is not in classpath of Blueberry-API, so we need to do this
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
            args.add(Joiner.on(";").join(classpath) + ";" + root.getAbsolutePath() + (dest != null ? ";" + dest.getAbsolutePath() : ""));
        }
        if (dest != null) {
            args.add("-d");
            args.add(dest.getAbsolutePath());
        }
        args.add(file.getAbsolutePath());
        Main.compile(args.toArray(new String[0]), new PrintWriter(SharedConstants.IS_RUNNING_IN_IDE ? new LoggedPrintStream("Blueberry Live Compiler", System.err) : new NoopPrintStream(), true));
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
        int nThreads = Math.max(4, Runtime.getRuntime().availableProcessors()) * 2;
        ExecutorService compilerExecutor = Executors.newFixedThreadPool(nThreads, new ThreadFactoryBuilder().setNameFormat("Blueberry Mod Compiler #%d").build());
        LOGGER.info("Compiling the source code using up to " + nThreads + " threads");
        AtomicBoolean first = new AtomicBoolean(true);
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
                            Runnable doCompile = () -> {
                                LOGGER.info("Compiling: " + path.relativize(f.toPath()).toString());
                                compile(file, f, tmp);
                                String rel = path.relativize(f.toPath()).toString().replaceAll("(.*)\\.java", "$1.class");
                                if (!new File(tmp, rel).exists()) {
                                    throwable.set(new RuntimeException("Compilation failed: " + rel));
                                    return;
                                }
                                LOGGER.debug("Compiled {} -> {}", f.getAbsolutePath(), tmp.getAbsolutePath());
                            };
                            if (first.get()) {
                                first.set(false);
                                doCompile.run();
                            } else {
                                compilerExecutor.submit(() -> {
                                    if (throwable.get() != null) return;
                                    doCompile.run();
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
                LOGGER.warn("Timed out compilation. Some compiled files may be missing.");
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
