package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.bml.config.RootCompoundVisualConfig;
import net.blueberrymc.common.bml.config.VisualConfig;
import net.blueberrymc.common.resources.BlueberryResourceManager;
import net.blueberrymc.config.ModConfig;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.common.launchwrapper.LaunchClassLoader;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BlueberryMod implements ModInfo {
    private Logger logger = LogManager.getLogger();
    private final ModStateList stateList = new ModStateList();
    private BlueberryModLoader modLoader;
    private ModDescriptionFile description;
    private ClassLoader classLoader;
    private ModConfig config;
    private RootCompoundVisualConfig visualConfig;
    private File file;
    private BlueberryResourceManager resourceManager;
    boolean first = true;
    boolean fromSource = false;
    @Nullable File sourceDir = null;

    public BlueberryMod() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        if (!(classLoader instanceof ModClassLoader)) {
            throw new IllegalStateException("BlueberryMod requires " + ModClassLoader.class.getCanonicalName() + " (Got " + classLoader.getClass().getCanonicalName() + " instead)");
        }
        try {
            ((ModClassLoader) classLoader).initialize(this);
        } catch (RuntimeException ex) {
            this.logger.fatal("Failed to initialize mod", ex);
            throw ex;
        }
    }

    @SuppressWarnings("unused")
    protected BlueberryMod(@NotNull BlueberryModLoader modLoader, @NotNull ModDescriptionFile description, @NotNull ClassLoader classLoader, @NotNull File file) {
        if (!ClassLoader.getSystemClassLoader().equals(this.getClass().getClassLoader()) && !(this.getClass().getClassLoader() instanceof LaunchClassLoader))
            throw new IllegalStateException("This constructor requires system class loader or LaunchClassLoader");
        this.getStateList().add(ModState.LOADED);
        init(modLoader, description, classLoader, file);
    }

    final void init(@NotNull BlueberryModLoader modLoader, @NotNull ModDescriptionFile description, @NotNull ClassLoader classLoader, @NotNull File file) {
        this.modLoader = modLoader;
        this.description = description;
        this.classLoader = classLoader;
        this.config = new ModConfig(this.description);
        this.logger = LogManager.getLogger(this.description.getName());
        this.visualConfig = new RootCompoundVisualConfig(new TextComponent(this.description.getName()));
        this.file = file;
        try {
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

    @Override
    @NotNull
    public final String getName() {
        return this.description.getName();
    }

    @Override
    @NotNull
    public final String getModId() {
        return this.description.getModId();
    }

    @NotNull
    public final Logger getLogger() {
        return logger;
    }

    @NotNull
    public final ModStateList getStateList() { return stateList; }

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
     * @param compoundVisualConfig the visual config
     */
    public void save(@NotNull("compoundVisualConfig") CompoundVisualConfig compoundVisualConfig) {
        for (VisualConfig<?> config : compoundVisualConfig) {
            if (config instanceof CompoundVisualConfig) {
                save((CompoundVisualConfig) config);
                continue;
            }
            if (config.getId() != null) {
                this.getConfig().set(config.getId(), config.get());
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
     * Called at very early stage of the mod loading, so you cannot use most minecraft classes here.
     */
    public void onLoad() {}

    /**
     * Called after the window is created. Do register items, blocks etc here.
     */
    public void onPreInit() {}

    /**
     * Called when the minecraft is initializing. (After started the rendering of LoadingOverlay)
     */
    public void onInit() {}

    /**
     * Called after the minecraft has finished loading.
     */
    public void onPostInit() {}

    /**
     * Called when the mod is being unloaded.
     */
    public void onUnload() {}
}
