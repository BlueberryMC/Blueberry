package net.blueberrymc.common.bml;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.blueberrymc.client.EarlyLoadingMessageManager;
import net.blueberrymc.client.commands.ClientCommandManager;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.launch.BlueberryPreBootstrap;
import net.blueberrymc.common.Side;
import net.blueberrymc.common.SideOnly;
import net.blueberrymc.common.bml.loading.ModLoadingError;
import net.blueberrymc.common.bml.loading.ModLoadingErrors;
import net.blueberrymc.common.resources.BlueberryResourceManager;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.common.util.ClasspathUtil;
import net.blueberrymc.common.util.FileUtil;
import net.blueberrymc.common.util.ListUtils;
import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.common.util.UniversalClassLoader;
import net.blueberrymc.common.util.VoidSafeExecutor;
import net.blueberrymc.common.util.tools.JavaTools;
import net.blueberrymc.common.util.tools.liveCompiler.JavaCompiler;
import net.blueberrymc.config.ModDescriptionFile;
import net.blueberrymc.config.yaml.YamlConfiguration;
import net.blueberrymc.server.main.ServerMain;
import net.blueberrymc.server.packs.resources.BlueberryResourceProvider;
import net.blueberrymc.util.Util;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class BlueberryModLoader implements ModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ConcurrentHashMap<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map.Entry<ModDescriptionFile, File>> descriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map.Entry<ModDescriptionFile, File>> filePath2descriptionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BlueberryMod> id2ModMap = new ConcurrentHashMap<>();
    private final Set<ClassLoader> loaders = new HashSet<>();
    private final List<BlueberryMod> registeredMods = new ArrayList<>();
    private final List<String> circularDependency = new ArrayList<>();
    private final File configDir = new File(Blueberry.getGameDir(), "config");
    private final File modsDir = new File(Blueberry.getGameDir(), "mods");
    @Nullable
    private UniversalClassLoader universalClassLoader = null;

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
        fillClasses(classes);
    }

    @Override
    public @NotNull List<BlueberryMod> getLoadedMods() {
        return ImmutableList.copyOf(registeredMods);
    }

    @Override
    public @Nullable BlueberryMod getModById(@NotNull String modId) {
        return id2ModMap.get(modId);
    }

    @Override
    public void loadMods() {
        ServerMain.blackboard.put("bml", this);
        Deque<File> toLoad = BlueberryPreBootstrap.lookForMods(modsDir);
        Map<String, File> fromSource = new HashMap<>();
        List<File> toAdd = new ArrayList<>();
        toLoad.forEach(file -> {
            try {
                Map.Entry<ModDescriptionFile, File> entry = preprocess(file);
                if (entry.getKey().isSource() && entry.getValue() != null) {
                    fromSource.put(entry.getKey().getModId(), file);
                    if (!entry.getValue().equals(file)) {
                        toLoad.remove(file);
                        toLoad.add(entry.getValue());
                    }
                }
            } catch (ModDescriptionNotFoundException ex) {
                LOGGER.warn("Adding into classpath from non-mod file/folder: " + file.getAbsolutePath() + ". This could cause severe issues, please remove it if possible.");
                toAdd.add(file);
            } catch (Throwable throwable) {
                LOGGER.error("Error during preprocessing {} (loaded from: {})", file.getName(), file.getAbsolutePath(), throwable);
                ModLoadingErrors.add(new ModLoadingError(new SimpleModInfo(file.getName(), file.getName()), "Error during preprocessing: " + throwable.getMessage(), false));
                Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                    @Override
                    public void execute() {
                        EarlyLoadingMessageManager.logError("Error during preprocessing " + file.getName() + " (loaded from: " + file.getAbsolutePath() + ")");
                    }
                });
                toLoad.remove(file);
            }
        });
        toAdd.forEach(file -> {
            toLoad.remove(file);
            try {
                addToUniversalClassLoader(file.toURI().toURL());
            } catch (Throwable e) {
                LOGGER.warn("Could not add into the classpath: {}", file.getAbsolutePath(), e);
            }
        });
        // pre-check before loading mods
        descriptions.forEach((modId, entry) -> {
            for (String depend : entry.getKey().getDepends()) {
                Map.Entry<ModDescriptionFile, File> dependDesc = descriptions.get(depend);
                if (dependDesc == null) {
                    toLoad.remove(entry.getValue());
                    String message = "Required dependency \"" + depend + "\" is missing";
                    LOGGER.error(modId + ": " + message);
                    ModLoadingErrors.add(new ModLoadingError(entry.getKey(), new UnknownModDependencyException(message), false));
                    Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                        @Override
                        public void execute() {
                            EarlyLoadingMessageManager.logError(modId + ": " + message);
                        }
                    });
                    continue;
                }
                if (!entry.getKey().getDepends().contains(dependDesc.getKey().getModId())) continue;
                if (!descriptions.get(depend).getKey().getDepends().contains(modId)) continue;
                circularDependency.add(modId);
                toLoad.remove(entry.getValue());
                ModLoadingErrors.add(new ModLoadingError(entry.getKey(), new InvalidModException("Circular dependency detected with " + depend), false));
                Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                    @Override
                    public void execute() {
                        EarlyLoadingMessageManager.logError("Circular dependency detected with " + depend);
                    }
                });
            }
        });
        if (!circularDependency.isEmpty()) {
            String deps = ListUtils.join(circularDependency, ", ");
            LOGGER.error("Following mods has circular dependency, cannot load: {}", deps);
            Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                @Override
                public void execute() {
                    EarlyLoadingMessageManager.logError("Following mods has circular dependency, cannot load: " + deps);
                }
            });
        }
        toLoad.forEach(file -> {
            try {
                BlueberryMod mod = this.loadMod(file);
                if (fromSource.containsKey(mod.getModId())) {
                    mod.fromSource = true;
                    mod.sourceDir = fromSource.get(mod.getModId());
                }
            } catch (InvalidModException ex) {
                ModDescriptionFile desc = filePath2descriptionMap.get(file.getAbsolutePath()).getKey();
                LOGGER.error("Could not load a mod (" + desc.getModId() + "): " + ex);
                ModLoadingErrors.add(new ModLoadingError(desc, "Could not load a mod: " + ex.getMessage(), false));
                Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                    @Override
                    public void execute() {
                        EarlyLoadingMessageManager.logError("Could not load a mod (" + desc.getModId() + "): " + ex.getMessage());
                    }
                });
            }
        });
    }

    @NotNull
    public Map.Entry<@NotNull ModDescriptionFile, @Nullable File> preprocess(@NotNull File file) throws IOException {
        ModDescriptionFile description = preloadMod(file);
        var entry = compileSource(file, description);
        if (entry != null) return entry;
        return new AbstractMap.SimpleImmutableEntry<>(description, null);
    }

    @Nullable
    public Map.Entry<ModDescriptionFile, File> compileSource(@NotNull File file, @NotNull ModDescriptionFile description) throws IOException {
        if (description.isSource()) {
            LOGGER.warn("Live Compiler is EXPERIMENTAL! Do not expect it to work.");
            if (!file.isDirectory()) {
                LOGGER.warn("source is true but the mod file is not a directory: {} ({}) [{}] (loaded from: {})", description.getName(), description.getModId(), description.getVersion(), file.getAbsolutePath());
                return new AbstractMap.SimpleImmutableEntry<>(description, null);
            }
            if (!JavaTools.isLoaded()) {
                LOGGER.warn(
                        "Not compiling source code of mod {} ({}) [{}] because live compiler is unavailable ({})",
                        description.getName(),
                        description.getModId(),
                        description.getVersion(),
                        JavaTools.UNAVAILABLE_REASON.getMessage()
                );
                ModLoadingErrors.add(new ModLoadingError(description, "Live compiler is unavailable: " + JavaTools.UNAVAILABLE_REASON.getMessage(), true));
                Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                    @Override
                    public void execute() {
                        EarlyLoadingMessageManager.logWarning("Live compiler is unavailable: " + JavaTools.UNAVAILABLE_REASON.getMessage());
                    }
                });
                return new AbstractMap.SimpleImmutableEntry<>(description, null);
            }
            LOGGER.info("Compiling the source code of mod {} ({}) [{}]", description.getName(), description.getModId(), description.getVersion());
            Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                @Override
                public void execute() {
                    EarlyLoadingMessageManager.logModLoader(String.format("Compiling the source code of mod %s (%s) [%s]", description.getName(), description.getModId(), description.getVersion()));
                }
            });
            File src = description.getSourceDir() != null ? new File(description.getSourceDir()) : file;
            if (!src.exists() || !src.isDirectory()) {
                src = new File(file, description.getSourceDir());
                if (!src.exists() || !src.isDirectory()) {
                    LOGGER.warn("Source dir does not exist or not a directory, using default one");
                    src = file;
                }
            }
            File finalSrc = src;
            try {
                File compiled = Util.waitUntilReturns("Blueberry Compiler Boss Thread", true, () -> {
                    try {
                        return JavaCompiler.compileAll(finalSrc);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                if (description.getInclude() != null) {
                    File include = new File(description.getInclude());
                    if (!include.exists() || !src.isDirectory()) {
                        include = new File(file, description.getInclude());
                        if (!include.exists() || !src.isDirectory()) {
                            LOGGER.warn("Include dir does not exist or not a directory, skipping");
                            Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                                @Override
                                public void execute() {
                                    EarlyLoadingMessageManager.logWarning("Include directory is missing or not a directory");
                                }
                            });
                        }
                    }
                    if (include.exists()) {
                        FileUtil.copy(include, compiled);
                    }
                }
                LOGGER.info("Successfully compiled the source code of mod {} ({})", description.getName(), description.getModId());
                Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                    @Override
                    public void execute() {
                        EarlyLoadingMessageManager.logModCompiler("Successfully compiled the source of mod " + description.getName() + " (" + description.getModId() + ")");
                    }
                });
                //filePath2descriptionMap.put(compiled.getAbsolutePath(), new AbstractMap.SimpleImmutableEntry<>(description, compiled));
                //descriptions.put(description.getModId(), new AbstractMap.SimpleImmutableEntry<>(description, compiled));
                descriptions.remove(description.getModId());
                return new AbstractMap.SimpleImmutableEntry<>(description, compiled);
            } catch (RuntimeException ex) {
                LOGGER.error("Failed to compile the source code of mod {} ({})", description.getName(), description.getModId());
                ModLoadingErrors.add(new ModLoadingError(description, "Failed to compile the source code", false));
                Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                    @Override
                    public void execute() {
                        EarlyLoadingMessageManager.logError("Failed to compile the source code of mod " + description.getName() + " (" + description.getModId() + ")");
                    }
                });
                ex.printStackTrace();
            }
        }
        return null;
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

    @Override
    public @NotNull ModDescriptionFile preloadMod(@NotNull File file) throws InvalidModDescriptionException {
        ModDescriptionFile description = getModDescription(file);
        if (description.getDepends().contains(description.getModId())) {
            ModLoadingErrors.add(new ModLoadingError(description, "Depends on itself", true));
            description.getDepends().remove(description.getModId());
        }
        if (descriptions.containsKey(description.getModId())) {
            ModDescriptionFile another = descriptions.get(description.getModId()).getKey();
            if (another != description) {
                throw new InvalidModDescriptionException("Duplicate mod ID: " + description.getModId() + "(Name: " + description.getName() + ") @ " + description.getVersion() + " and " + another.getModId() + " (Name: " + another.getName() + ") @ " + another.getVersion());
            }
        }
        Map.Entry<ModDescriptionFile, File> entry = new AbstractMap.SimpleImmutableEntry<>(description, file);
        filePath2descriptionMap.put(file.getAbsolutePath(), entry);
        descriptions.put(description.getModId(), entry);
        return description;
    }

    @Override
    @NotNull
    public BlueberryMod loadMod(@NotNull File file) throws InvalidModException {
        return this.loadMod(file, null);
    }

    @NotNull
    public BlueberryMod loadMod(@NotNull File file, @Nullable File sourceDir) throws InvalidModException {
        Preconditions.checkNotNull(file, "file cannot be null");
        Preconditions.checkArgument(file.exists(), file.getAbsolutePath() + " does not exist");
        Map.Entry<ModDescriptionFile, File> entry = filePath2descriptionMap.get(file.getAbsolutePath());
        if (entry == null) {
            throw new InvalidModException("The mod " + file.getAbsolutePath() + " has no preloaded data exists");
        }
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
            } catch (Exception throwable) {
                throw new InvalidModException("Failed to load dependency of the mod '" + description.getModId() + "': " + depend, throwable);
            }
        }
        if (!noDescription.isEmpty()) {
            throw new InvalidModException("Missing dependencies of the mod '" + description.getModId() + "': " + ListUtils.join(noDescription, ", "));
        }
        for (String depend : description.getSoftDepends()) {
            if (!descriptions.containsKey(depend)) continue;
            if (id2ModMap.containsKey(depend)) continue;
            try {
                loadMod(descriptions.get(depend).getValue());
            } catch (Exception throwable) {
                LOGGER.warn(new InvalidModException("Failed to load (soft) dependency of the mod '" + description.getModId() + "': " + depend, throwable));
            }
        }
        try {
            LOGGER.info("Loading mod {} ({}) version {}", description.getName(), description.getModId(), description.getVersion());
            Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                @Override
                public void execute() {
                    EarlyLoadingMessageManager.logModLoader(String.format("Loading mod %s (%s) version %s", description.getName(), description.getModId(), description.getVersion()));
                }
            });
            ModClassLoader modClassLoader = new ModClassLoader(this, this.getClass().getClassLoader(), description, file);
            loaders.add(modClassLoader);
            BlueberryMod mod = modClassLoader.mod;
            registeredMods.add(mod);
            id2ModMap.put(description.getModId(), mod);
            if (sourceDir != null) {
                mod.fromSource = true;
                mod.sourceDir = sourceDir;
            }
            LOGGER.info("Loaded mod {} ({}) version {}", description.getName(), description.getModId(), description.getVersion());
            Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                @Override
                public void execute() {
                    EarlyLoadingMessageManager.logModLoader(String.format("Loaded mod %s (%s) version %s", description.getName(), description.getModId(), description.getVersion()));
                }
            });
            return mod;
        } catch (IOException ex) {
            throw new InvalidModException(ex);
        }
    }

    @Override
    public void enableMod(@NotNull BlueberryMod mod) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        if (mod.getClassLoader() instanceof ModClassLoader && ((ModClassLoader) mod.getClassLoader()).isClosed()) {
            throw new IllegalArgumentException("ClassLoader is already closed (unregistered?)");
        }
        if (mod.getStateList().getCurrentState() == ModState.AVAILABLE) throw new IllegalArgumentException("Mod " + mod.getModId() + " is already enabled");
        try {
            mod.doEnable();
        } catch (Throwable throwable) {
            LOGGER.error("Failed to enable a mod {} ({}) [{}]", mod.getName(), mod.getDescription().getModId(), mod.getDescription().getVersion(), throwable);
            mod.getStateList().add(ModState.ERRORED);
        }
        loaders.add(mod.getClassLoader());
        if (mod.getStateList().getCurrentState() == ModState.AVAILABLE) {
            LOGGER.info("Enabled mod " + mod.getDescription().getModId());
        }
    }

    @Override
    public void disableMod(@NotNull BlueberryMod mod) {
        disableMod(mod, false);
    }

    @Override
    public void disableMod(@NotNull BlueberryMod mod, boolean unregister) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        Preconditions.checkArgument(mod.getStateList().getCurrentState() != ModState.UNLOADED, "mod already unloaded");
        if (!unregister && !Blueberry.stopping && !mod.getDescription().isUnloadable()) throw new IllegalArgumentException(mod.getName() + " (" + mod.getModId() + ") cannot be unloaded");
        try {
            mod.onUnload();
            mod.getStateList().add(ModState.UNLOADED);
            Blueberry.getEventManager().unregisterEvents(mod);
            Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
                @Override
                public void execute() {
                    ClientCommandManager.unregisterAll(mod);
                }
            });
        } catch (Throwable throwable) {
            LOGGER.error("Failed to unload a mod {} ({}) [{}]", mod.getName(), mod.getDescription().getModId(), mod.getDescription().getVersion(), throwable);
        }
        loaders.remove(mod.getClassLoader());
        if (unregister) {
            if (mod.getClassLoader() instanceof ModClassLoader) {
                try {
                    ((ModClassLoader) mod.getClassLoader()).close();
                } catch (IOException ex) {
                    LOGGER.warn("Error closing class loader '{}' of mod {} ({}) [{}]", mod.getClassLoader().getClass().getSimpleName(), mod.getName(), mod.getModId(), mod.getDescription().getVersion(), ex);
                }
            }
            {
                AtomicReference<String> toRemove = new AtomicReference<>();
                filePath2descriptionMap.forEach((s, entry) -> {
                    if (entry.getKey().getModId().equals(mod.getModId())) {
                        toRemove.set(s);
                    }
                });
                if (toRemove.get() != null) {
                    filePath2descriptionMap.remove(toRemove.get());
                }
            }
            this.descriptions.remove(mod.getModId());
            this.id2ModMap.remove(mod.getModId());
            this.registeredMods.remove(mod);
            this.loaders.remove(mod.getClassLoader());
            try {
                BlueberryResourceManager blueberryResourceManager = mod.getResourceManager();
                ResourceManager resourceManager = Blueberry.getUtil().getResourceManager();
                if (resourceManager instanceof BlueberryResourceProvider provider) {
                    provider.remove(blueberryResourceManager.getPackResources());
                } else {
                    LOGGER.warn("Unknown ResourceManager type: " + resourceManager.getClass().getTypeName());
                }
                blueberryResourceManager.getPackResources().close();
                Blueberry.getUtil().reloadResourcePacks(); // reload to apply changes
            } catch (Exception ex) {
                LOGGER.warn("Error unregistering ResourceManager", ex);
            }
            {
                List<String> toRemove = new ArrayList<>();
                this.classes.forEach((s, c) -> {
                    if (c.getClassLoader() instanceof ModClassLoader mcl && mcl.mod.getModId().equals(mod.getModId())) {
                        toRemove.add(s);
                    }
                });
                toRemove.forEach(this.classes::remove);
            }
        }
        LOGGER.info("Disabled mod {} ({}) [{}]", mod.getName(), mod.getModId(), mod.getDescription().getVersion());
    }

    @Override
    @NotNull
    public ModDescriptionFile getModDescription(@NotNull File file) throws InvalidModDescriptionException {
        Preconditions.checkNotNull(file, "file cannot be null");
        try {
            try (ModFile modFile = new ModFile(file);
                 InputStream in = modFile.getResourceAsStream("mod.yml")) {
                if (in == null) throw new ModDescriptionNotFoundException(file.getName() + " does not contain mod.yml");
                return ModDescriptionFile.read(new YamlConfiguration(in).asObject());
            }
        } catch (IOException ex) {
            throw new InvalidModDescriptionException(ex);
        }
    }

    private void precheckRegisterMod(@NotNull ModDescriptionFile description) {
        AtomicBoolean cancel = new AtomicBoolean(false);
        if (description.getDepends().contains(description.getModId())) {
            ModLoadingErrors.add(new ModLoadingError(description, "Depends on itself (check mod.yml)", true));
            description.getDepends().remove(description.getModId());
        }
        for (String depend : description.getDepends()) {
            Map.Entry<ModDescriptionFile, File> dependDesc = descriptions.get(depend);
            if (dependDesc == null) {
                String message = "Required dependency \"" + depend + "\" is missing (download the mod, and put on mods folder)";
                LOGGER.error(description.getModId() + ": " + message);
                ModLoadingErrors.add(new ModLoadingError(description, new UnknownModDependencyException(message), false));
                cancel.set(true);
                continue;
            }
            if (!description.getDepends().contains(dependDesc.getKey().getModId())) continue;
            if (!descriptions.get(depend).getKey().getDepends().contains(description.getModId())) continue;
            circularDependency.add(description.getModId());
            cancel.set(true);
            ModLoadingErrors.add(new ModLoadingError(description, new InvalidModException("Circular dependency detected with " + depend + " (check mod.yml and resolve circular dependency)"), false));
        }
        if (cancel.get()) throw new InvalidModException("Could not register mod " + description.getModId());
        if (!circularDependency.isEmpty()) {
            throw new InvalidModException("Following mods has circular dependency, cannot load: " + ListUtils.join(circularDependency, ", "));
        }
    }

    public void registerInternalBlueberryMod(@NotNull ModDescriptionFile description) {
        forceRegisterMod(description, new InternalBlueberryMod(this, description, Launch.classLoader, new File(ClasspathUtil.getClasspath(InternalBlueberryMod.class))));
    }

    @NotNull
    public <T extends BlueberryMod> T forceRegisterMod(@NotNull ModDescriptionFile description, @NotNull T mod) throws InvalidModException {
        precheckRegisterMod(description);
        descriptions.put(description.getModId(), new AbstractMap.SimpleImmutableEntry<>(description, null));
        id2ModMap.put(description.getModId(), mod);
        registeredMods.add(mod);
        LOGGER.info("Loaded mod {} ({}) from class {}", mod.getName(), mod.getDescription().getModId(), mod.getClass().getTypeName());
        return mod;
    }

    @NotNull
    @Override
    public BlueberryMod forceRegisterMod(@NotNull ModDescriptionFile description, boolean useModClassLoader) throws InvalidModException {
        precheckRegisterMod(description);
        BlueberryMod mod;
        Class<?> clazz;
        try {
            clazz = Class.forName(description.getMainClass(), false, Launch.classLoader);
        } catch (ClassNotFoundException ex) {
            throw new InvalidModException(ex);
        }
        String path = ClasspathUtil.getClasspath(clazz);
        LOGGER.debug("Class Path of " + clazz.getTypeName() + ": " + path);
        File file = new File(path);
        if (useModClassLoader) {
            ModClassLoader modClassLoader;
            try {
                modClassLoader = new ModClassLoader(this, this.getClass().getClassLoader(), description, file);
            } catch (Throwable ex) {
                throw new InvalidModException(ex);
            }
            loaders.add(modClassLoader);
            mod = modClassLoader.mod;
        } else {
            try {
                Constructor<?> constructor = clazz
                        .getDeclaredConstructor(BlueberryModLoader.class, ModDescriptionFile.class, ClassLoader.class, File.class);
                constructor.setAccessible(true);
                mod = (BlueberryMod) constructor.newInstance(this, description, this.getClass().getClassLoader(), file);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new InvalidModException(e);
            }
        }
        descriptions.put(description.getModId(), new AbstractMap.SimpleImmutableEntry<>(description, null));
        id2ModMap.put(description.getModId(), mod);
        registeredMods.add(mod);
        LOGGER.info("Loaded mod {} ({}) from class {}", mod.getName(), mod.getDescription().getModId(), clazz.getTypeName());
        return mod;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initModResources(@NotNull BlueberryMod mod) {
        BlueberryResourceManager blueberryResourceManager = new BlueberryResourceManager(mod);
        mod.setResourceManager(blueberryResourceManager);
        ResourceManager resourceManager = Blueberry.getUtil().getResourceManager();
        if (resourceManager instanceof ReloadableResourceManager rm) {
            CloseableResourceManager crm = (CloseableResourceManager) ReflectionHelper.getFieldWithoutException(ReloadableResourceManager.class, rm, "resources");
            if (crm instanceof MultiPackResourceManager) {
                List<PackResources> packs = (List<PackResources>) ReflectionHelper.getFieldWithoutException(MultiPackResourceManager.class, crm, "packs");
                assert packs != null;
                packs.add(blueberryResourceManager.getPackResources());
            }
        } else if (resourceManager instanceof MultiPackResourceManager rm) {
            List<PackResources> packs = (List<PackResources>) ReflectionHelper.getFieldWithoutException(MultiPackResourceManager.class, rm, "packs");
            assert packs != null;
            packs.add(blueberryResourceManager.getPackResources());
        } else if (resourceManager instanceof FallbackResourceManager rm) {
            rm.add(blueberryResourceManager.getPackResources());
        } else {
            LOGGER.warn("Unknown ResourceManager type: " + resourceManager.getClass().getTypeName());
        }
    }

    @Override
    public void callPreInit() {
        LOGGER.info("Entered Pre-init phase");
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                EarlyLoadingMessageManager.logModLoader("# Pre-init");
            }
        });
        getActiveMods().forEach(mod -> {
            try {
                mod.getStateList().add(ModState.PRE_INIT);
                initModResources(mod);
                mod.onPreInit();
            } catch (Throwable throwable) {
                mod.getStateList().add(ModState.ERRORED);
                Blueberry.crash(Blueberry.pauseInIde(throwable), "Pre Initialization of " + mod.getName() + " (" + mod.getDescription().getModId() + ")");
            }
        });
    }

    @Override
    public void callInit() {
        LOGGER.info("Entered Init phase");
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                EarlyLoadingMessageManager.logModLoader("# Init");
            }
        });
        getActiveMods().forEach(mod -> {
            try {
                mod.getStateList().add(ModState.INIT);
                mod.onInit();
            } catch (Throwable throwable) {
                mod.getStateList().add(ModState.ERRORED);
                Blueberry.crash(throwable, "Initialization of " + mod.getName() + " (" + mod.getDescription().getModId() + ")");
            }
        });
    }

    @Override
    public void callPostInit() {
        LOGGER.info("Entered Post-init phase");
        Blueberry.safeRunOnClient(() -> new VoidSafeExecutor() {
            @Override
            public void execute() {
                EarlyLoadingMessageManager.logModLoader("# Post-init");
            }
        });
        getActiveMods().forEach(mod -> {
            if (mod.getStateList().contains(ModState.AVAILABLE)) return;
            try {
                mod.getStateList().add(ModState.POST_INIT);
                mod.onPostInit();
                mod.first = false;
                mod.getStateList().add(ModState.AVAILABLE);
            } catch (Throwable throwable) {
                mod.getStateList().add(ModState.ERRORED);
                Blueberry.crash(throwable, "Post Initialization of " + mod.getName() + " (" + mod.getDescription().getModId() + ")");
            }
        });
    }

    @Nullable
    @Override
    public InputStream getResourceAsStream(@NotNull String name) {
        InputStream in = ModLoader.super.getResourceAsStream(name);
        if (in != null) return in;
        if (universalClassLoader == null) return null;
        return universalClassLoader.getResourceAsStream(name);
    }

    @Nullable
    public Class<?> findClass(@NotNull String name) {
        Class<?> result = classes.get(name);
        if (result != null) return result;
        for (ClassLoader loader : loaders) {
            try {
                if (loader instanceof ModClassLoader mcl) {
                    result = mcl.findClass(name, false);
                } else {
                    result = loader.loadClass(name);
                }
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

    private void addToUniversalClassLoader(@NotNull URL url) {
        if (universalClassLoader == null) {
            universalClassLoader = new UniversalClassLoader(new URL[]{url});
            loaders.add(universalClassLoader);
        } else {
            universalClassLoader.addURL(url);
        }
    }

    private static void fillClasses(Map<String, Class<?>> classes) {
        classes.put("net.minecraftforge.fml.relauncher.Side", Side.class);
        classes.put("net.minecraftforge.fml.relauncher.SideOnly", SideOnly.class);
        classes.put("net.blueberrymc.client.resources.BlueberryText", BlueberryText.class);
    }
}
