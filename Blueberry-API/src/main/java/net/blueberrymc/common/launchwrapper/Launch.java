package net.blueberrymc.common.launchwrapper;

import java.io.File;
import java.lang.reflect.Method;
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
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;

public class Launch {
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    public static File minecraftHome;
    public static File assetsDir;
    public static final Map<String, Object> blackboard = new HashMap<>();
    public static LaunchClassLoader classLoader;

    public static void main(String[] args) {
        (new Launch()).launch(args);
    }

    private Launch() {
        ClassLoader cl = this.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            classLoader = new LaunchClassLoader(((URLClassLoader) cl).getURLs(), null);
        } else {
            classLoader = new LaunchClassLoader(new URL[0], cl);
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
                    LogWrapper.log(Level.WARN, "Tweak class name %s has already been visited -- skipping", tweakName);
                } else {
                    allTweakerNames.add(tweakName);
                    LogWrapper.log(Level.INFO, "Loading tweak class name %s", tweakName);
                    classLoader.addClassLoaderExclusion(tweakName.substring(0, tweakName.lastIndexOf(46)));
                    ITweaker tweaker = (ITweaker)Class.forName(tweakName, true, classLoader).newInstance();
                    tweakers.add(tweaker);
                    if (primaryTweaker == null) {
                        LogWrapper.log(Level.INFO, "Using primary tweak class name %s", tweakName);
                        primaryTweaker = tweaker;
                    }
                }
            }

            tweakers.forEach(tweaker -> {
                LogWrapper.log(Level.INFO, "Calling tweak class %s", tweaker.getClass().getName());
                tweaker.acceptOptions(options.valuesOf(nonOption), minecraftHome, assetsDir, profileName);
                // TODO
                //tweaker.injectIntoClassLoader(classLoader);
                allTweakers.add(tweaker);
            });

            allTweakers.forEach(tweaker -> argumentList.addAll(Arrays.asList(tweaker.getLaunchArguments())));

            String launchTarget = Objects.requireNonNull(primaryTweaker).getLaunchTarget();
            Class<?> clazz = Class.forName(launchTarget, false, classLoader);
            Method mainMethod = clazz.getMethod("main", String[].class);
            LogWrapper.info("Launching wrapped minecraft {%s}", launchTarget);
            mainMethod.invoke(null, (Object) argumentList.toArray(new String[0]));
        } catch (Exception e) {
            LogWrapper.log(Level.ERROR, e, "Unable to launch");
            System.exit(1);
        }
    }
}
