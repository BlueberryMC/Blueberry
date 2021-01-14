package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.resources.BlueberryResourceManager;
import net.blueberrymc.common.bml.config.RootCompoundVisualConfig;
import net.blueberrymc.common.util.ListUtils;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.config.yaml.YamlConfiguration;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class BlueberryModLoader implements ModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ConcurrentHashMap<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map.Entry<ModDescriptionFile, File>> descriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map.Entry<ModDescriptionFile, File>> filePath2descriptionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BlueberryMod> id2ModMap = new ConcurrentHashMap<>();
    private final List<ModClassLoader> loaders = new ArrayList<>();
    private final List<BlueberryMod> registeredMods = new ArrayList<>();
    private final List<String> circularDependency = new ArrayList<>();
    private final File parent = new File(".");
    private final File configDir = new File(parent, "config");
    private final File modsDir = new File(parent, "mods");

    public BlueberryModLoader() {
        if (!this.configDir.exists() && !this.configDir.mkdir()) {
            LOGGER.warn("Could not create config directory");
        }
        if (this.configDir.isFile()) {
            throw new IllegalStateException("config directory is not a directory");
        }
        if (!this.modsDir.exists() && !this.modsDir.mkdir()) {
            LOGGER.warn("Could not create mods directory");
        }
        if (this.modsDir.isFile()) {
            throw new IllegalStateException("mods directory is not a directory");
        }
    }

    @Override
    public @NotNull List<BlueberryMod> getLoadedMods() {
        return ImmutableList.copyOf(registeredMods);
    }

    @Override
    public void loadMods() {
        LOGGER.info("Looking for mods in " + this.getModsDir().getAbsolutePath());
        List<File> toLoad = new ArrayList<>();
        int dirCount = 0;
        int fileCount = 0;
        File[] files = this.getModsDir().listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals(Versioning.getVersion().getGameVersion())) {
                    for (File f : file.listFiles()) {
                        if (f.isDirectory()) {
                            File descriptionFile = new File(f, "mod.yml");
                            if (descriptionFile.exists()) {
                                if (descriptionFile.isDirectory()) {
                                    LOGGER.warn(descriptionFile.getAbsolutePath() + " exists but is not a file");
                                } else {
                                    dirCount++;
                                    toLoad.add(f);
                                }
                            }
                        } else {
                            if (f.getName().equals(".zip") || f.getName().equals(".jar")) {
                                fileCount++;
                                toLoad.add(f);
                            }
                        }
                    }
                }
                File descriptionFile = new File(file, "mod.yml");
                if (descriptionFile.exists()) {
                    if (descriptionFile.isDirectory()) {
                        LOGGER.warn(descriptionFile.getAbsolutePath() + " exists but is not a file");
                    } else {
                        dirCount++;
                        toLoad.add(file);
                    }
                }
            } else {
                if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
                    fileCount++;
                    toLoad.add(file);
                }
            }
        }
        LOGGER.info("Found " + toLoad.size() + " mods to load (files: {}, directories: {})", fileCount, dirCount);
        toLoad.forEach(this::preloadMod);
        ImmutableMap.copyOf(descriptions).forEach((s, entry) -> {
            for (String depend : entry.getKey().getDepends()) {
                if (!entry.getKey().getDepends().contains(descriptions.get(depend).getKey().getModId())) continue;
                if (!descriptions.get(depend).getKey().getDepends().contains(s)) continue;
                circularDependency.add(s);
            }
        });
        if (!circularDependency.isEmpty()) {
            LOGGER.error("Following mods has circular dependency, cannot load: {}", ListUtils.join(circularDependency, ", "));
        }
        toLoad.forEach(file -> {
            try {
                this.loadMod(file);
            } catch (InvalidModException ex) {
                LOGGER.error("Could not load a mod: " + ex);
            }
        });
    }

    @Override
    @NotNull
    public File getConfigDir() {
        return configDir;
    }

    @Override
    @NotNull
    public File getModsDir() {
        return modsDir;
    }

    private void preloadMod(@NotNull File file) throws InvalidModException {
        ModDescriptionFile description;
        try {
            description = getModDescription(file);
            if (description.getDepends().contains(description.getModId())) {
                throw new InvalidModDescriptionException("Cannot contain itself on dependencies");
            }
        } catch (InvalidModDescriptionException ex) {
            throw new InvalidModException(ex);
        }
        Map.Entry<ModDescriptionFile, File> entry = new AbstractMap.SimpleImmutableEntry<>(description, file);
        filePath2descriptionMap.put(file.getAbsolutePath(), entry);
        descriptions.put(description.getModId(), entry);
    }

    @Override
    @NotNull
    public BlueberryMod loadMod(@NotNull File file) throws InvalidModException {
        Preconditions.checkNotNull(file, "file cannot be null");
        Map.Entry<ModDescriptionFile, File> entry = filePath2descriptionMap.get(file.getAbsolutePath());
        ModDescriptionFile description = entry.getKey();
        if (description == null) throw new InvalidModException(new AssertionError("ModDescriptionFile of " + file.getAbsolutePath() + " could not be found"));
        if (circularDependency.contains(description.getModId())) throw new InvalidModException("Mod '" + description.getModId() + "' has circular dependency");
        if (id2ModMap.containsKey(description.getModId())) return id2ModMap.get(description.getModId());
        List<String> noDescription = new ArrayList<>();
        for (String depend : description.getDepends()) {
            if (!descriptions.containsKey(depend)) {
                noDescription.add(depend);
                continue;
            }
            if (id2ModMap.containsKey(depend)) continue;
            try {
                loadMod(descriptions.get(depend).getValue());
            } catch (Throwable throwable) {
                throw new InvalidModException("Failed to load dependency of the mod '" + description.getModId() + "': " + depend, throwable);
            }
        }
        if (!noDescription.isEmpty()) {
            throw new InvalidModException("Missing dependencies of the mod '" + description.getModId() + "': " + ListUtils.join(noDescription, ", "));
        }
        try {
            LOGGER.info("Loading mod {} ({}) version {}", description.getName(), description.getModId(), description.getVersion());
            ModClassLoader modClassLoader = new ModClassLoader(this, this.getClass().getClassLoader(), description, file);
            loaders.add(modClassLoader);
            BlueberryMod mod = modClassLoader.mod;
            registeredMods.add(mod);
            id2ModMap.put(description.getModId(), mod);
            LOGGER.info("Loaded mod {} ({}) version {}", description.getName(), description.getModId(), description.getVersion());
            return mod;
        } catch (IOException ex) {
            throw new InvalidModException(ex);
        }
    }

    @Override
    public void enableMod(@NotNull BlueberryMod mod) {
        try {
            mod.getStateList().add(ModState.LOADED);
            mod.setVisualConfig(new RootCompoundVisualConfig(new TextComponent(mod.getName())));
            mod.onLoad();
            mod.getStateList().add(ModState.PRE_INIT);
            mod.onPreInit();
            mod.getStateList().add(ModState.INIT);
            mod.onInit();
            mod.getStateList().add(ModState.POST_INIT);
            mod.onPostInit();
            mod.getStateList().add(ModState.AVAILABLE);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to enable a mod {} ({}) [{}]", mod.getName(), mod.getDescription().getModId(), mod.getDescription().getVersion(), throwable);
        }
        if (mod.hasClassLoader()) {
            loaders.add(mod.getClassLoader());
        }
        LOGGER.info("Enabled mod " + mod.getDescription().getModId());
    }

    @Override
    public void disableMod(@NotNull BlueberryMod mod) {
        try {
            mod.onUnload();
            mod.getStateList().add(ModState.UNLOADED);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to unload a mod {} ({}) [{}]", mod.getName(), mod.getDescription().getModId(), mod.getDescription().getVersion(), throwable);
        }
        if (mod.hasClassLoader()) {
            loaders.remove(mod.getClassLoader());
        }
        LOGGER.info("Disabled mod " + mod.getDescription().getModId());
    }

    @Override
    @NotNull
    public ModDescriptionFile getModDescription(@NotNull File file) throws InvalidModDescriptionException {
        Preconditions.checkNotNull(file, "file cannot be null");
        if (file.isFile()) {
            return readModDescriptionFromFile(file);
        } else {
            return readModDescriptionFromDirectory(file);
        }
    }

    @NotNull
    public ModDescriptionFile readModDescriptionFromFile(@NotNull File file) throws InvalidModDescriptionException {
        if (!file.isFile()) throw new InvalidModDescriptionException(file.getName() + " is not a file");
        JarFile jar = null;
        InputStream in = null;
        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("mod.yml");
            if (entry == null) {
                throw new InvalidModDescriptionException(new FileNotFoundException(file.getName() + " does not contain mod.yml"));
            }
            in = jar.getInputStream(entry);
            return ModDescriptionFile.read(new YamlConfiguration(in).asObject());
        } catch (IOException | YAMLException | ClassCastException | NullPointerException ex) {
            throw new InvalidModDescriptionException(ex);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ignore) {}
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {}
            }
        }
    }

    @NotNull
    public ModDescriptionFile readModDescriptionFromDirectory(@NotNull File file) throws InvalidModDescriptionException {
        if (!file.isDirectory()) throw new InvalidModDescriptionException(file.getName() + " is not a directory");
        File entry = new File(file, "mod.yml");
        if (!entry.exists()) throw new InvalidModDescriptionException(file.getName() + " does not contain mod.yml");
        if (!entry.isFile()) throw new InvalidModDescriptionException("mod.yml is not a file");
        if (!entry.canRead()) throw new InvalidModDescriptionException("Cannot read " + entry.getName() + " - not enough permission?");
        FileInputStream in = null;
        try {
            return ModDescriptionFile.read(new YamlConfiguration(in = new FileInputStream(entry)).asObject());
        } catch (IOException | YAMLException | ClassCastException | NullPointerException ex) {
            throw new InvalidModDescriptionException(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {}
            }
        }
    }

    @SuppressWarnings({ "unchecked", "RedundantCast" })
    @NotNull
    @Override
    public <T extends BlueberryMod> T forceRegisterMod(@NotNull ModDescriptionFile description, @NotNull Class<T> clazz) throws InvalidModException {
        LOGGER.info("Loading mod {} ({}) from class {}", description.getName(), description.getModId(), clazz.getCanonicalName());
        /*
        if (id2ModMap.containsKey(description.getModId())) {
            LOGGER.warn("...But it was already cached, returning them.");
            return (T) id2ModMap.get(description.getModId());
        }
        */
        ModClassLoader modClassLoader = null;
        if (!SharedConstants.IS_RUNNING_IN_IDE) {
            try {
                modClassLoader = new ModClassLoader(this, this.getClass().getClassLoader(), description, new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            } catch (Throwable ex) {
                throw new InvalidModException(ex);
            }
            loaders.add(modClassLoader);
        }
        BlueberryMod mod;
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            try {
                mod = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new InvalidModException(e);
            }
        } else {
            mod = modClassLoader.mod;
        }
        try {
            ((T) mod).getClass().getClassLoader();
        } catch (ClassCastException ex) {
            loaders.remove(modClassLoader);
            throw new InvalidModException(ex);
        }
        descriptions.put(description.getModId(), new AbstractMap.SimpleImmutableEntry<>(description, null));
        id2ModMap.put(description.getModId(), mod);
        registeredMods.add(mod);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            mod.getStateList().add(ModState.LOADED);
            try {
                mod.init(this, description, modClassLoader, new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            } catch (URISyntaxException e) {
                throw new AssertionError("should not really happen :(");
            }
        }
        mod.setDescription(description);
        LOGGER.info("Loaded mod {} ({}) from class {}", mod.getName(), mod.getDescription().getModId(), clazz.getCanonicalName());
        return (T) mod;
    }

    @Override
    public void callPreInit() {
        LOGGER.info("Entered Pre-init phase");
        getActiveMods().forEach(mod -> {
            try {
                mod.getStateList().add(ModState.PRE_INIT);
                BlueberryResourceManager blueberryResourceManager = new BlueberryResourceManager(mod);
                mod.setResourceManager(blueberryResourceManager);
                ResourceManager resourceManager = Blueberry.getUtil().getResourceManager();
                if (resourceManager instanceof SimpleReloadableResourceManager) {
                    ((SimpleReloadableResourceManager) resourceManager).add(blueberryResourceManager.getPackResources());
                } else if (resourceManager instanceof FallbackResourceManager) {
                    ((FallbackResourceManager) resourceManager).add(blueberryResourceManager.getPackResources());
                } else {
                    if (resourceManager != null) {
                        LOGGER.warn("Failed to add PackResources for ResourceManager: " + resourceManager.getClass().getCanonicalName());
                    } else {
                        if (Blueberry.isClient()) {
                            LOGGER.warn("ResourceManager is null!", new NullPointerException());
                        }
                    }
                }
                mod.onPreInit();
            } catch (Throwable throwable) {
                mod.getStateList().add(ModState.ERRORED);
                Blueberry.crash(throwable, "Pre Initialization of " + mod.getName() + " (" + mod.getDescription().getModId() + ")");
            }
        });
    }

    @Override
    public void callInit() {
        LOGGER.info("Entered Init phase");
        getActiveMods().forEach(mod -> {
            try {
                mod.getStateList().add(ModState.INIT);
                mod.onInit();
            } catch (Throwable throwable) {
                mod.getStateList().add(ModState.ERRORED);
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Initialization of " + mod.getName() + " (" + mod.getDescription().getModId() + ")");
                Minecraft.fillReport(null, null, null, crashReport);
                Minecraft.crash(crashReport);
            }
        });
    }

    @Override
    public void callPostInit() {
        LOGGER.info("Entered Post-init phase");
        getActiveMods().forEach(mod -> {
            if (mod.getStateList().contains(ModState.AVAILABLE)) return;
            try {
                mod.getStateList().add(ModState.POST_INIT);
                mod.onPostInit();
                mod.getStateList().add(ModState.AVAILABLE);
            } catch (Throwable throwable) {
                mod.getStateList().add(ModState.ERRORED);
                Blueberry.crash(throwable, "Post Initialization of " + mod.getName() + " (" + mod.getDescription().getModId() + ")");
            }
        });
    }

    @Nullable
    protected Class<?> findClass(@NotNull String name) {
        Class<?> result = classes.get(name);
        if (result != null) return result;
        for (ModClassLoader loader : loaders) {
            try {
                result = loader.findClass(name, false);
            } catch (ClassNotFoundException ignore) {}
            if (result != null) {
                setClass(name, result);
                return result;
            }
        }
        return null;
    }

    protected void setClass(@NotNull String name, @NotNull Class<?> clazz) {
        if (!classes.containsKey(name)) {
            classes.put(name, clazz);
        }
    }
}
