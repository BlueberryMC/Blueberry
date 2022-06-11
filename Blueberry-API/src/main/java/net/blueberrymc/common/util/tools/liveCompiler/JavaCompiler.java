package net.blueberrymc.common.util.tools.liveCompiler;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonDeserializer;
import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.types.Type;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.AsciiHeadersEncoder;
import io.netty.util.AttributeKey;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.blueberrymc.client.EarlyLoadingMessageManager;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.util.ClasspathUtil;
import net.blueberrymc.common.util.VoidSafeExecutor;
import net.blueberrymc.nativeutil.NativeUtil;
import net.blueberrymc.util.ThreadLocalLoggedBufferedOutputStream;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class JavaCompiler {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Set<String> classpath;

    static {
        Set<String> cp = new HashSet<>();
        cp.add(ClasspathUtil.getClasspath(Blueberry.class)); // Blueberry-API
        cp.add(ClasspathUtil.getClasspath(Nonnull.class)); // javax
        cp.add(ClasspathUtil.getClasspath(Launch.class)); // Launch Wrapper
        cp.add(ClasspathUtil.getClasspath(ClassVisitor.class));// ASM
        cp.add(ClasspathUtil.getClasspath(Mixin.class)); // Mixin
        cp.add(ClasspathUtil.getClasspath(ImmutableMap.class)); // Guava
        cp.add(ClasspathUtil.getClasspath(Float2FloatOpenHashMap.class)); // fastutil
        cp.add(ClasspathUtil.getClasspath(StringUtils.class)); // commons-lang3
        cp.add(ClasspathUtil.getClasspath(JsonDeserializer.class)); // Gson
        cp.add(ClasspathUtil.getClasspath(Type.class)); // DataFixerUpper
        cp.add(ClasspathUtil.getClasspath(Message.class)); // Brigadier
        cp.add(ClasspathUtil.getClasspath(GameVersion.class)); // javabridge
        cp.add(ClasspathUtil.getClasspath(NotNull.class)); // jetbrains annotations
        cp.add(ClasspathUtil.getClasspath(AttributeKey.class)); // netty-common
        cp.add(ClasspathUtil.getClasspath(AsciiHeadersEncoder.class)); // netty-codec
        cp.add(ClasspathUtil.getClasspath(Channel.class)); // netty-transport
        cp.add(ClasspathUtil.getClasspath(ByteBuf.class)); // netty-buffer
        cp.add(ClasspathUtil.getClasspath(NativeUtil.class)); // NativeUtil
        cp.add(ClasspathUtil.getClasspath(Logger.class)); // Log4j2
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                try {
                    cp.add(ClasspathUtil.getClasspath(Class.forName("com.mojang.blaze3d.vertex.PoseStack"))); // Blaze3d
                    cp.add(ClasspathUtil.getClasspath(org.lwjgl.glfw.GLFW.class)); // LWJGL
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            // these class are not in classpath of Blueberry-API, so we need to do this
            cp.add(ClasspathUtil.getClasspath(Class.forName("org.apache.commons.io.IOUtils"))); // commons-io
            cp.add(ClasspathUtil.getClasspath(Class.forName("net.minecraft.server.MinecraftServer"))); // Minecraft
            cp.add(ClasspathUtil.getClasspath(Class.forName("net.minecraft.client.gui.ScreenManager"))); // MinecraftForge-API
        } catch (ClassNotFoundException e) {
            if (Blueberry.isClient()) {
                e.printStackTrace();
            }
        }
        classpath = ImmutableSet.copyOf(cp);
        LOGGER.info("Classpath for compiler: " + Joiner.on(File.pathSeparator).join(classpath));
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
            args.add(Joiner.on(File.pathSeparator).join(classpath) + File.pathSeparator + root.getAbsolutePath());
        }
        if (dest != null) {
            args.add("-d");
            args.add(dest.getAbsolutePath());
        }
        args.add("-proc:none");
        args.add("-source");
        args.add("17");
        args.add(file.getAbsolutePath());
        OutputStream out = new ThreadLocalLoggedBufferedOutputStream("Blueberry Live Compiler", Level.WARN);
        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new RuntimeException("JavaCompiler is not available");
        int result = compiler.run(System.in, out, out, args.toArray(new String[0]));
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
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                EarlyLoadingMessageManager.logModCompiler("Compiling the source code using up to " + nThreads + " threads");
            }
        });
        AtomicBoolean first = new AtomicBoolean(true);
        try (Stream<Path> stream = Files.walk(file.toPath())) {
            stream.map(Path::toFile)
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
                                    Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                                        @Override
                                        public void execute() {
                                            EarlyLoadingMessageManager.logModCompiler("Compiling: " + rel);
                                        }
                                    });
                                    compile(file, f, tmp);
                                    if (!new File(tmp, rel).exists()) {
                                        throwable.set(new RuntimeException("Compilation failed: " + rel));
                                        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                                            @Override
                                            public void execute() {
                                                EarlyLoadingMessageManager.logModCompiler("Failed to compile: " + rel);
                                            }
                                        });
                                        LOGGER.error("Failed to compile: " + rel);
                                        return;
                                    }
                                    LOGGER.debug("Compiled {} -> {}", f.getAbsolutePath(), tmp.getAbsolutePath());
                                    Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                                        @Override
                                        public void execute() {
                                            EarlyLoadingMessageManager.logModCompiler("Compiled: " + rel);
                                        }
                                    });
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
                                            Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                                                @Override
                                                public void execute() {
                                                    EarlyLoadingMessageManager.logModCompiler("Failed to compile: " + rel);
                                                }
                                            });
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
        }
        compilerExecutor.shutdown();
        try {
            if (!compilerExecutor.awaitTermination(5L, TimeUnit.MINUTES)) {
                LOGGER.warn("Timed out compilation. Some files may be missing.");
                Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                    @Override
                    public void execute() {
                        EarlyLoadingMessageManager.logModCompiler("Timed out compilation. Some files may be missing.");
                    }
                });
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
