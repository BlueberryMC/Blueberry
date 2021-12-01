package net.blueberrymc.client.main;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.blueberrymc.common.util.FileUtil;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientMain {
    private static final Logger LOGGER = LogManager.getLogger();
    public static File tempModDir = null;

    public static void main(@NotNull String@NotNull[] args) throws IOException {
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
        OptionSet set = optionParser.parse(args); // use args because we don't need --tweakClass here
        if (set.has("debug")) SharedConstants.IS_RUNNING_IN_IDE = true;
        if (!set.has("assetsDir")) {
            LOGGER.info("Auto-detected .minecraft directory: " + FileUtil.getMinecraftDir().getAbsolutePath());
            arguments.add("--assetsDir=" + new File(FileUtil.getMinecraftDir(), "assets").getAbsolutePath() + "");
        }
        for (String sourceDir : set.valuesOf(sourceDirOption)) {
            if (tempModDir == null) tempModDir = Files.createTempDirectory("blueberry-temp-mod-dir-").toFile();
            try {
                FileUtil.copy(new File(sourceDir), tempModDir);
            } catch (IOException ex) {
                LOGGER.warn("Failed to copy {} -> {}/", sourceDir, tempModDir.getAbsolutePath(), ex);
            }
        }
        for (String includeDir : set.valuesOf(includeDirOption)) {
            if (tempModDir == null) {
                LOGGER.warn("Not copying {} because sourceDir was missing", includeDir);
            } else {
                try {
                    FileUtil.copy(new File(includeDir), tempModDir);
                } catch (IOException ex) {
                    LOGGER.warn("Failed to copy {} -> {}/", includeDir, tempModDir.getAbsolutePath(), ex);
                }
            }
        }
        String[] newArgs = arguments.toArray(new String[0]);
        BlueberryClientTweaker.args = newArgs;
        Launch.main(newArgs);
    }

    private static void preloadClasses() {
        preloadClass("com.sun.jna.Structure");
        preloadClass("com.sun.jna.platform.win32.WinNT$OSVERSIONINFOEX");
        preloadClass("com.sun.jna.platform.win32.VersionHelpers");
        //preloadClass("net.blueberrymc.client.world.level.fluid.FluidSpriteManager");
    }

    private static void preloadClass(String clazz) {
        try {
            Class.forName(clazz);
        } catch (ClassNotFoundException ignore) {}
    }
}
