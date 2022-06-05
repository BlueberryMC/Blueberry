package net.blueberrymc.server.main;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.launch.BlueberryPreBootstrap;
import net.blueberrymc.common.launch.BlueberryTweaker;
import net.blueberrymc.common.util.FileUtil;
import net.blueberrymc.nativeutil.NativeUtil;
import net.blueberrymc.util.Util;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServerMain {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Map<String, Object> blackboard = new Object2ObjectOpenHashMap<>();
    @Nullable
    public static File tempModDir;

    public static void main(@NotNull String@NotNull[] args) throws IOException {
        List<String> arguments = new ArrayList<>();
        arguments.add("--tweakClass=net.blueberrymc.server.main.BlueberryServerTweaker");
        arguments.addAll(Arrays.asList(args));
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("debug");
        OptionSpec<String> sourceDirOption = optionParser.accepts("sourceDir").withRequiredArg();
        OptionSpec<String> includeDirOption = optionParser.accepts("includeDir").withRequiredArg();
        OptionSpec<File> gameDirOption = optionParser.accepts("universe").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSet set = optionParser.parse(args); // use args because we don't need --tweakClass here
        List<String> illegalPackages = new ArrayList<>();
        illegalPackages.add("net/minecraft/client/");
        illegalPackages.add("net/blueberrymc/client/");
        illegalPackages.add("net/blueberrymc/common/bml/client/");
        illegalPackages.add("net/blueberrymc/mixin/client/");
        try {
            NativeUtil.registerClassLoadHook((classLoader, name, clazz, protectionDomain, bytes) -> {
                for (String illegalPackage : illegalPackages) {
                    if (name.startsWith(illegalPackage)) {
                        throw new RuntimeException("Trying to load invalid class '" + name + "' from server side");
                    }
                }
                return null;
            });
        } catch (Throwable t) {
            LOGGER.error("Failed to register class load hook; illegal packages will be able to load", t);
        }
        ServerMain.launch(Side.SERVER, arguments, set, gameDirOption, sourceDirOption, includeDirOption);
    }

    public static void launch(@NotNull Side side, @NotNull List<String> arguments, @NotNull OptionSet set, @NotNull OptionSpec<File> universeOption, @NotNull OptionSpec<String> sourceDirOption, @NotNull OptionSpec<String> includeDirOption) throws IOException {
        File universe = Objects.requireNonNull(Util.parseArgument(set, universeOption));
        //SharedConstants.tryDetectVersion();
        for (String sourceDir : set.valuesOf(sourceDirOption)) {
            if (tempModDir == null) {
                tempModDir = Files.createTempDirectory("blueberry-temp-mod-dir-").toFile();
                LOGGER.info("Created temp directory {}", tempModDir.getAbsolutePath());
            }
            try {
                FileUtil.copy(new File(sourceDir), tempModDir);
                LOGGER.info("Copied source dir: {} -> {}/", sourceDir, tempModDir.getAbsolutePath());
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
                    LOGGER.info("Copied include dir: {} -> {}/", includeDir, tempModDir.getAbsolutePath());
                } catch (IOException ex) {
                    LOGGER.warn("Failed to copy {} -> {}/", includeDir, tempModDir.getAbsolutePath(), ex);
                }
            }
        }
        String[] newArgs = arguments.toArray(new String[0]);
        BlueberryTweaker.args = newArgs;
        blackboard.put("side", side.name());
        blackboard.put("universe", universe);
        blackboard.put("debug", set.has("debug"));
        BlueberryPreBootstrap.preBootstrap(side, universe);
        Launch.main(newArgs);
    }
}
