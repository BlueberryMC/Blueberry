package net.minecraft.launchwrapper;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryModLoader;
import net.blueberrymc.common.util.ClasspathUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

// TODO: fix classes are not loading using LaunchClassLoader
public class Launch {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    public static File minecraftHome;
    public static File assetsDir;
    public static final Map<String, Object> blackboard = new HashMap<>();
    public static LaunchClassLoader classLoader;

    public static void main(@NotNull String@NotNull[] args) {
        new Launch().launch(args);
    }

    private Launch() {
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl instanceof URLClassLoader ucl) {
            classLoader = new LaunchClassLoader(ucl.getURLs(), null);
        } else {
            try {
                Set<URL> cp = ClasspathUtil.collectClasspathAsURL();
                if (Blueberry.getModLoader() instanceof BlueberryModLoader bml) {
                    bml.addURLsToSet(cp);
                }
                classLoader = new LaunchClassLoader(cp.toArray(new URL[0]), cl);
            } catch (MalformedURLException e) {
                LOGGER.warn("Failed to create LaunchClassLoader with path: {}", ClasspathUtil.getClasspath(Launch.class), e);
                classLoader = new LaunchClassLoader(new URL[0], cl);
            }
        }
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private void launch(String[] args) {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        OptionSpec<String> profileOption = parser.accepts("version", "The version we launched with").withRequiredArg();
        OptionSpec<File> gameDirOption = parser.accepts("gameDir", "Alternative game directory").withRequiredArg().ofType(File.class);
        OptionSpec<File> assetsDirOption = parser.accepts("assetsDir", "Assets directory").withRequiredArg().ofType(File.class);
        OptionSpec<String> tweakClassOption = parser.accepts("tweakClass", "Tweak class(es) to load").withRequiredArg().defaultsTo(DEFAULT_TWEAK);
        OptionSpec<String> nonOption = parser.nonOptions();
        OptionSet options = parser.parse(args);
        minecraftHome = options.valueOf(gameDirOption);
        assetsDir = options.valueOf(assetsDirOption);
        String profileName = options.valueOf(profileOption);
        List<String> tweakClassNames = new ArrayList<>(options.valuesOf(tweakClassOption));
        List<String> argumentList = new ArrayList<>();
        blackboard.put("TweakClasses", tweakClassNames);
        blackboard.put("ArgumentList", argumentList);
        Set<String> allTweakerNames = new HashSet<>();
        ArrayList<ITweaker> allTweakers = new ArrayList<>();

        try {
            List<ITweaker> tweakers = new ArrayList<>(tweakClassNames.size() + 1);
            blackboard.put("Tweaks", tweakers);
            ITweaker primaryTweaker = null;

            for (String tweakName : tweakClassNames) {
                if (allTweakerNames.contains(tweakName)) {
                    LOGGER.warn("Tweak class name {} has already been visited -- skipping", tweakName);
                } else {
                    allTweakerNames.add(tweakName);
                    LOGGER.info("Loading tweak class name {}", tweakName);
                    classLoader.addClassLoaderExclusion(tweakName.substring(0, tweakName.lastIndexOf(46)));
                    ITweaker tweaker = (ITweaker)Class.forName(tweakName, true, classLoader).getDeclaredConstructor().newInstance();
                    tweakers.add(tweaker);
                    if (primaryTweaker == null) {
                        LOGGER.info("Using primary tweak class name {}", tweakName);
                        primaryTweaker = tweaker;
                    }
                }
            }

            tweakers.forEach(tweaker -> {
                LOGGER.info("Calling tweak class {}", tweaker.getClass().getName());
                tweaker.acceptOptions(options.valuesOf(nonOption), minecraftHome, assetsDir, profileName);
                tweaker.injectIntoClassLoader(classLoader);
                allTweakers.add(tweaker);
            });

            allTweakers.forEach(tweaker -> argumentList.addAll(Arrays.asList(tweaker.getLaunchArguments())));

            if (Blueberry.getModLoader() instanceof BlueberryModLoader bml) {
                bml.destroyUniversalClassLoader();
            }
            String launchTarget = Objects.requireNonNull(primaryTweaker).getLaunchTarget();
            Class<?> clazz = Class.forName(launchTarget, false, classLoader);
            Method mainMethod = clazz.getMethod("main", String[].class);
            LOGGER.info("Launching wrapped minecraft {}", launchTarget);
            mainMethod.invoke(null, (Object) argumentList.toArray(new String[0]));
        } catch (Exception e) {
            LOGGER.error("Unable to launch", e);
            System.exit(1);
        }
    }
}
