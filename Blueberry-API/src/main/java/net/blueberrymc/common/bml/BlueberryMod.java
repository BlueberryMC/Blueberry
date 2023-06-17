package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.bml.config.RootCompoundVisualConfig;
import net.blueberrymc.common.bml.config.VisualConfig;
import net.blueberrymc.common.resources.BlueberryResourceManager;
import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.config.ModConfig;
import net.blueberrymc.config.ModDescriptionFile;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.RecordComponent;
import java.net.URL;
import java.util.function.Function;

public class BlueberryMod implements VersionedModInfo {
    static {
        if (!(BlueberryMod.class.getClassLoader() instanceof LaunchClassLoader)) {
            throw new AssertionError("BlueberryMod loaded from wrong class loader (should be LaunchClassLoader): " + BlueberryMod.class.getClassLoader());
        }
    }

    private Logger logger = LogManager.getLogger();
    private final ModStateList stateList = new ModStateList();
    private BlueberryModLoader modLoader;
    private ModDescriptionFile description;
    private ClassLoader classLoader;
    private ModConfig config;
    private RootCompoundVisualConfig visualConfig;
    private File file;
    private BlueberryResourceManager resourceManager;
    boolean first = Blueberry.getCurrentState() != ModState.AVAILABLE;
    boolean fromSource = false;
    @Nullable File sourceDir = null;

    public BlueberryMod() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (!(classLoader instanceof ModClassLoader)) {
            throw new IllegalStateException("BlueberryMod requires " + ModClassLoader.class.getTypeName() + " (Got " + classLoader.getClass().getTypeName() + " instead)");
        }
        try {
            ((ModClassLoader) classLoader).initialize(this);
        } catch (RuntimeException ex) {
            this.logger.fatal("Failed to initialize mod", ex);
            throw ex;
        }
    }

    /**
     * This constructor normally cannot be used by mods, don't use it.
     * @param modLoader mod loader
     * @param description mod description file
     * @param classLoader actual class loader
     * @param file mod file (.jar file or directory)
     */
    @SuppressWarnings("unused")
    protected BlueberryMod(@NotNull BlueberryModLoader modLoader, @NotNull ModDescriptionFile description, @NotNull ClassLoader classLoader, @NotNull File file) {
        if (!ClassLoader.getSystemClassLoader().equals(this.getClass().getClassLoader()) && !(this.getClass().getClassLoader() instanceof LaunchClassLoader))
            throw new IllegalStateException("This constructor requires system class loader or LaunchClassLoader");
        this.getStateList().add(ModState.LOADED);
        init(modLoader, description, classLoader, file);
    }

    final void doEnable() {
        getStateList().add(ModState.LOADED);
        setVisualConfig(new RootCompoundVisualConfig(Component.literal(getName())));
        onLoad();
        getStateList().add(ModState.PRE_INIT);
        onPreInit();
        getStateList().add(ModState.INIT);
        onInit();
        getStateList().add(ModState.POST_INIT);
        onPostInit();
        getStateList().add(ModState.AVAILABLE);
    }

    final void init(@NotNull BlueberryModLoader modLoader, @NotNull ModDescriptionFile description, @NotNull ClassLoader classLoader, @NotNull File file) {
        try {
            this.modLoader = modLoader;
            this.description = description;
            this.classLoader = classLoader;
            this.config = new ModConfig(this.description);
            this.logger = LogManager.getLogger(this.description.getName());
            this.file = file;
            this.visualConfig = new RootCompoundVisualConfig(Component.literal(this.description.getName()));
            this.onLoad();
        } catch (Throwable throwable) {
            this.stateList.add(ModState.ERRORED);
            Blueberry.crash(throwable, "Loading mod " + this.description.getName() + " (" + this.description.getModId() + ")");
        }
    }

    /**
     * Use this method to check whether the first initialization (meaning minecraft is loading) is in progress.
     */
    protected final boolean isFirst() {
        return first;
    }

    /**
     * Add a URL to ModClassLoader.
     * @param url url to add
     * @throws UnsupportedOperationException if mod's classLoader is not instance of ModClassLoader
     */
    protected void addURLToClassLoader(@NotNull URL url) {
        if (classLoader instanceof ModClassLoader mcl) {
            mcl.addURL(url);
        } else {
            throw new UnsupportedOperationException("classLoader is not instance of ModClassLoader");
        }
    }

    /**
     * Checks if the mod is unloaded. This method is equivalent to: <code>getStateList().getCurrentState() == ModState.UNLOADED;</code>
     * @return true if mod is unloaded
     */
    public final boolean isUnloaded() {
        return getStateList().getCurrentState() == ModState.UNLOADED;
    }

    @NotNull
    public final BlueberryModLoader getModLoader() {
        return modLoader;
    }

    @NotNull
    public final ModDescriptionFile getDescription() {
        return description;
    }

    @NotNull
    public final ClassLoader getClassLoader() {
        if (classLoader == null) throw new AssertionError("classLoader should not be null!");
        return classLoader;
    }

    @NotNull
    @Override
    public final String getName() {
        return this.description.getName();
    }

    @NotNull
    @Override
    public final String getModId() {
        return this.description.getModId();
    }

    @NotNull
    @Override
    public final String getVersion() {
        return this.description.getVersion();
    }

    /**
     * Returns the logger for the mod.
     * @return logger
     */
    @NotNull
    public final Logger getLogger() {
        return logger;
    }

    @NotNull
    public final ModStateList getStateList() {
        return stateList;
    }

    /**
     * Returns the mod config.
     * @return mod config
     */
    @NotNull
    public final ModConfig getConfig() {
        return config;
    }

    @NotNull
    public final File getFile() {
        return file;
    }

    @NotNull
    public final RootCompoundVisualConfig getVisualConfig() {
        return visualConfig;
    }

    public final void setVisualConfig(@NotNull RootCompoundVisualConfig visualConfig) {
        Preconditions.checkNotNull(visualConfig, "cannot set null VisualConfig");
        this.visualConfig = visualConfig;
    }

    public final void setResourceManager(@NotNull BlueberryResourceManager resourceManager) {
        Preconditions.checkNotNull(resourceManager, "ResourceManager cannot be null");
        this.resourceManager = resourceManager;
    }

    @NotNull
    public final BlueberryResourceManager getResourceManager() {
        if (resourceManager == null) throw new IllegalArgumentException("ResourceManager is not defined (yet)");
        return resourceManager;
    }

    /**
     * Saves the configuration file. {@link VisualConfig#id(String)} must be called with valid config path to work.
     * <p><strong>NOTE: You cannot save the config which were generated with VisualConfigManager.</strong> Use
     * {@link net.blueberrymc.common.bml.config.VisualConfigManager#save(ModConfig, CompoundVisualConfig)} for that.
     * @param compoundVisualConfig the visual config
     */
    public void save(@NotNull CompoundVisualConfig compoundVisualConfig) {
        save(compoundVisualConfig, o -> {
            if (o instanceof Class<?> clazz) {
                return clazz.getTypeName();
            }
            return o;
        });
    }

    /**
     * Saves the configuration file. {@link VisualConfig#id(String)} must be called with valid config path to work.
     * <p><strong>NOTE: You cannot save the config which were generated with VisualConfigManager.</strong> Use
     * {@link net.blueberrymc.common.bml.config.VisualConfigManager#save(ModConfig, CompoundVisualConfig)} for that.
     * @param compoundVisualConfig the visual config
     * @param valueMapper see source code of {@link #save(CompoundVisualConfig)} for example
     */
    public void save(@NotNull CompoundVisualConfig compoundVisualConfig, @NotNull Function<Object, Object> valueMapper) {
        for (VisualConfig<?> config : compoundVisualConfig) {
            if (config instanceof CompoundVisualConfig) {
                save((CompoundVisualConfig) config);
                continue;
            }
            if (config.getId() != null) {
                this.getConfig().set(config.getId(), valueMapper.apply(config.get()));
            }
        }
    }

    public boolean isFromSource() {
        return fromSource;
    }

    @Nullable
    public File getSourceDir() {
        return sourceDir;
    }

    /**
     * Called at very early stage of the mod loading, you cannot use most minecraft classes here.
     */
    public void onLoad() {}

    /**
     * Called after the window is created. Do register items, blocks etc.
     */
    public void onPreInit() {}

    /**
     * Called when the minecraft is initializing. (Called at the beginning of rendering of LoadingOverlay)
     */
    public void onInit() {}

    /**
     * Called after the minecraft has finished loading.
     */
    public void onPostInit() {}

    /**
     * Called when the mod is being unloaded. Use {@link Blueberry#isStopping()} to distinguish between recompile and shutdown.
     */
    public void onUnload() {}

    /**
     * Called when the mod is being reloaded via mod list screen or via command.
     * @return Set to true if you want to reload resources, false otherwise.
     */
    public boolean onReload() {
        return false;
    }

    /**
     * Detects the mod from a class.
     * @param clazz the class
     * @return detected mod; null if mod could not be detected
     */
    @Nullable
    public static BlueberryMod detectModFromClass(@NotNull Class<?> clazz) {
        if (clazz.getClassLoader() instanceof ModClassLoader mcl) {
            return mcl.getMod();
        }
        if (ClassLoader.getSystemClassLoader().equals(clazz.getClassLoader()) || (clazz.getClassLoader() instanceof LaunchClassLoader)) {
            return Blueberry.getModLoader().getModById("blueberry");
        }
        return null;
    }

    /**
     * Detects the mod from an {@link java.lang.reflect.AnnotatedElement}.
     * @param element the element
     * @return detected mod; null if mod could not be detected
     */
    @Nullable
    public static BlueberryMod detectModFromElement(@NotNull AnnotatedElement element) {
        if (element instanceof Class<?> clazz) {
            return detectModFromClass(clazz);
        } else if (element instanceof Member member) {
            return detectModFromClass(member.getDeclaringClass());
        } else if (element instanceof RecordComponent component) {
            return detectModFromClass(component.getDeclaringRecord());
        }
        return null;
    }

    /**
     * Detects the mod from a caller class.
     * @return detected mod; null if mod could not be detected
     */
    @Nullable
    public static BlueberryMod getCallerMod() {
        return detectModFromClass(ReflectionHelper.getCallerClass());
    }
}
