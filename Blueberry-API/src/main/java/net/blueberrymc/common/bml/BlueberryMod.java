package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.config.RootCompoundVisualConfig;
import net.blueberrymc.common.resources.BlueberryResourceManager;
import net.blueberrymc.config.ModConfig;
import net.blueberrymc.config.ModDescriptionFile;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class BlueberryMod {
    volatile static boolean isFile = false;
    private Logger logger = LogManager.getLogger();
    private final ModStateList stateList = new ModStateList();
    private BlueberryModLoader modLoader;
    private ModDescriptionFile description;
    private ModClassLoader classLoader;
    private ModConfig config;
    private RootCompoundVisualConfig visualConfig;
    private File file;
    private BlueberryResourceManager resourceManager;

    public BlueberryMod() {
        if (!SharedConstants.IS_RUNNING_IN_IDE || isFile) {
            ClassLoader classLoader = this.getClass().getClassLoader();
            if (!(classLoader instanceof ModClassLoader)) {
                throw new IllegalStateException("BlueberryMod requires " + ModClassLoader.class.getCanonicalName() + " (Got " + classLoader.getClass().getCanonicalName() + " instead)");
            }
            try {
                ((ModClassLoader) classLoader).initialize(this);
            } catch (RuntimeException ex) {
                this.logger.error("Failed to initialize mod", ex);
                ex.printStackTrace();
                throw ex;
            }
        }
    }

    final void init(BlueberryModLoader modLoader, ModDescriptionFile description, ModClassLoader classLoader, File file) {
        if (classLoader == null && !SharedConstants.IS_RUNNING_IN_IDE) {
            throw new IllegalArgumentException("Cannot initialize mods with null classLoader");
        }
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

    void setDescription(ModDescriptionFile description) {
        this.description = description;
    }

    public boolean isUnloaded() {
        return getStateList().getCurrentState() == ModState.UNLOADED;
    }

    @NotNull
    public BlueberryModLoader getModLoader() {
        return modLoader;
    }

    @NotNull
    public ModDescriptionFile getDescription() {
        return description;
    }

    @NotNull
    public ModClassLoader getClassLoader() {
        if (classLoader == null && SharedConstants.IS_RUNNING_IN_IDE) {
            throw new UnsupportedOperationException("Not available in debug environment");
        } else {
            if (classLoader == null) throw new AssertionError("classLoader should not be null!");
            return classLoader;
        }
    }

    public boolean hasClassLoader() {
        return classLoader != null;
    }

    /**
     * @deprecated internal usage only
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Nullable
    @Deprecated
    public ModClassLoader getRawClassLoader() {
        return classLoader;
    }

    @NotNull
    public String getName() {
        return this.description.getName();
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    public ModStateList getStateList() { return stateList; }

    @NotNull
    public ModConfig getConfig() {
        return config;
    }

    @NotNull
    public File getFile() {
        return file;
    }

    @NotNull
    public RootCompoundVisualConfig getVisualConfig() {
        return visualConfig;
    }

    public void setVisualConfig(@NotNull RootCompoundVisualConfig visualConfig) {
        Preconditions.checkNotNull(visualConfig, "cannot set null VisualConfig");
        this.visualConfig = visualConfig;
    }

    public void setResourceManager(BlueberryResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public BlueberryResourceManager getResourceManager() {
        if (resourceManager == null) throw new IllegalArgumentException("ResourceManager is not defined (yet)");
        return resourceManager;
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
