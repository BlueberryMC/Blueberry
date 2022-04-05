package net.blueberrymc.common.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.types.Type;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.blueberrymc.common.bml.InvalidModException;
import net.blueberrymc.server.main.ServerMain;
import net.blueberrymc.util.OSType;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.ClassVisitor;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ClasspathUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static @NotNull Set<String> collectClasspath() {
        Set<String> cp = new HashSet<>();
        cp.add(ClasspathUtil.getClasspath(ServerMain.class)); // Blueberry-API
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
            // these classes are not in classpath of Blueberry-API, so we need to do this
            cp.add(ClasspathUtil.getClasspath(Class.forName("net.minecraft.client.gui.ScreenManager"))); // MinecraftForge-API
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Could not find class of MinecraftForge-API", e);
        }
        return cp;
    }

    public static @NotNull Set<URL> collectClasspathAsURL() throws MalformedURLException {
        Set<URL> urls = new HashSet<>();
        for (String s : collectClasspath()) {
            urls.add(pathToURL(s));
        }
        return urls;
    }

    public static @NotNull URL pathToURL(@NotNull String path) throws MalformedURLException {
        MalformedURLException ex;
        try {
            return new File(path).toURI().toURL();
        } catch (MalformedURLException exception) {
            ex = exception;
        }
        path = "file:" + path;
        try {
            return new URL(path);
        } catch (MalformedURLException exception) {
            exception.addSuppressed(ex);
            throw exception;
        }
    }

    public static @NotNull URL getClasspathAsURL(@NotNull Class<?> clazz) throws MalformedURLException {
        return pathToURL(getClasspath(clazz));
    }

    public static @NotNull String getClasspath(@NotNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "class cannot be null");
        String path;
        try {
            path = clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if (path == null) {
                path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
                if (path.startsWith("file:")) path = path.substring(5);
            }
        } catch (URISyntaxException e) {
            throw new InvalidModException(e);
        }
        if (path.matches("^(.*\\.jar)!.*$")) path = path.replaceAll("^(.*\\.jar)!.*$", "$1");
        path = path.replace("\\", "/");
        if (!path.endsWith(".jar")) {
            if (clazz.getPackage() != null) {
                path = path.replace(clazz.getPackage().getName().replace(".", "/"), "");
                path = path.replaceAll("(.*)/.*\\.class", "$1");
            } else {
                path = path.replace(clazz.getTypeName().replace(".", "/") + ".class", "");
            }
        }
        if (path.endsWith("/") || path.endsWith("\\")) path = path.substring(0, path.length() - 1);
        if (OSType.detectOS() == OSType.Windows && path.matches("^/[A-Z]:/.*$")) {
            path = path.substring(1).replace("/", "\\");
        }
        return path;
    }
}
