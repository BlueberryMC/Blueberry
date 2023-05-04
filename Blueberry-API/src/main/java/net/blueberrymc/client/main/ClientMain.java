package net.blueberrymc.client.main;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.util.FileUtil;
import net.blueberrymc.server.main.ServerMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientMain {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(@NotNull String@NotNull[] args) throws IOException {
        org.lwjgl.glfw.GLFW.glfwInit();
        List<String> arguments = new ArrayList<>();
        arguments.add("--tweakClass=net.blueberrymc.client.main.BlueberryClientTweaker");
        arguments.addAll(Arrays.asList(args));
        preloadClasses();
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("debug");
        optionParser.accepts("assetsDir");
        OptionSpec<String> sourceDirOption = optionParser.accepts("sourceDir").withRequiredArg();
        OptionSpec<String> includeDirOption = optionParser.accepts("includeDir").withRequiredArg();
        OptionSpec<File> gameDirOption = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSet set = optionParser.parse(args); // use args because we don't need --tweakClass here
        if (!set.has("assetsDir")) {
            LOGGER.info("Auto-detected .minecraft directory: " + FileUtil.getMinecraftDir().getAbsolutePath());
            arguments.add("--assetsDir=" + new File(FileUtil.getMinecraftDir(), "assets").getAbsolutePath());
        }
        ServerMain.launch(Side.CLIENT, arguments, set, gameDirOption, sourceDirOption, includeDirOption);
    }

    private static void preloadClasses() {
        preloadClass("com.sun.jna.Structure");
        preloadClass("com.sun.jna.platform.win32.WinNT$OSVERSIONINFOEX");
        preloadClass("com.sun.jna.platform.win32.VersionHelpers");
    }

    private static void preloadClass(String clazz) {
        try {
            Class.forName(clazz);
        } catch (ClassNotFoundException ignore) {}
    }
}
