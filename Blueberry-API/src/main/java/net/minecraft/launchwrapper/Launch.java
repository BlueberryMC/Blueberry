package net.minecraft.launchwrapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.blueberrymc.common.launch.BlueberryPreBootstrap;
import net.blueberrymc.common.util.ClasspathUtil;
import net.blueberrymc.nativeutil.NativeUtil;
import net.blueberrymc.server.main.ServerMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
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

public class Launch {
    private static final Logger LOGGER = LogManager.getLogger("LaunchWrapper");
    private static final String DEFAULT_TWEAK = "net.minecraft.launchwrapper.VanillaTweaker";
    public static File minecraftHome;
    public static File assetsDir;
    public static final Map<String, Object> blackboard = new HashMap<>();
    public static LaunchClassLoader classLoader;
    private static Throwable lastError;

    public static void main(@NotNull String@NotNull[] args) {
        new Launch().launch(args);
    }

    private Launch() {
        ClassLoader cl = this.getClass().getClassLoader();
        LOGGER.info("ClassLoader: {} / {}", cl, cl.getClass().getTypeName());
        if (cl instanceof URLClassLoader ucl) {
            classLoader = new LaunchClassLoader(ucl.getURLs(), null);
        } else {
            Set<URL> cp = new HashSet<>(Arrays.asList(tryGetURLsFromAppClassLoader(cl)));
            try {
                if (cp.isEmpty()) {
                    cp.add(ClasspathUtil.getClasspathAsURL(ServerMain.class));
                }
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
            if (lastError != null) {
                LOGGER.warn("Could not get URLs from AppClassLoader", lastError);
            }
            BlueberryPreBootstrap.addURLsToSet(cp);
            LOGGER.info("Classpath for LaunchClassLoader:");
            for (URL url : cp) {
                LOGGER.info(" - \"{}\"", url);
            }
            classLoader = new LaunchClassLoader(cp.toArray(new URL[0]), cl);
        }
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Contract(value = "null -> new", pure = true)
    @NotNull
    private static URL@NotNull[] tryGetURLsFromAppClassLoader(@Nullable ClassLoader cl) {
        // ton of hacks to get the classpath from the AppClassLoader
        if (cl == null) {
            lastError = new RuntimeException("cl is null");
            return new URL[0];
        }
        Field field;
        try {
            field = cl.getClass().getDeclaredField("ucp");
        } catch (NoSuchFieldException e) {
            try {
                field = cl.getClass().getSuperclass().getDeclaredField("ucp");
            } catch (NoSuchFieldException | NullPointerException ex) {
                ex.addSuppressed(e);
                lastError = ex;
                return new URL[0];
            }
        }
        Object ucp = NativeUtil.getObject(field, cl);
        if (ucp == null) {
            lastError = new RuntimeException("ucp is null");
            return new URL[0];
        }
        Method getURLs;
        try {
            getURLs = ucp.getClass().getMethod("getURLs");
        } catch (NoSuchMethodException e) {
            lastError = e;
            return new URL[0];
        }
        Object urls = NativeUtil.invokeObject(getURLs, ucp);
        if (urls == null) {
            lastError = new RuntimeException("getURLs() is null");
            return new URL[0];
        }
        return (URL[]) urls;
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

            BlueberryPreBootstrap.destroyUniversalClassLoader();
            String launchTarget = Objects.requireNonNull(primaryTweaker).getLaunchTarget();
            // Class<?> clazz = Class.forName(launchTarget, false, classLoader);
            Class<?> clazz = Class.forName(launchTarget);
            LOGGER.info("Loaded class {} from {}", clazz.getTypeName(), ClasspathUtil.getClasspath(clazz));
            Method mainMethod = clazz.getMethod("main", String[].class);
            LOGGER.info("Launching wrapped minecraft {}", launchTarget);
            mainMethod.invoke(null, (Object) argumentList.toArray(new String[0]));
        } catch (Exception e) {
            LOGGER.error("Unable to launch", e);
            System.exit(1);
        }
    }
}
