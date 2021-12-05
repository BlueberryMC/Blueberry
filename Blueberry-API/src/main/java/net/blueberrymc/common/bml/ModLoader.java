package net.blueberrymc.common.bml;

import com.google.common.collect.ImmutableList;
import net.blueberrymc.config.ModDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface ModLoader {
    /**
     * Returns the list of registered mods.
     * @return mods list
     */
    @NotNull
    List<BlueberryMod> getLoadedMods();

    @SuppressWarnings("unchecked")
    @NotNull
    default <T extends BlueberryMod> Optional<T> getModByClass(@NotNull Class<T> clazz) {
        return (Optional<T>) getLoadedMods().stream().filter(clazz::isInstance).findFirst();
    }

    @Nullable
    BlueberryMod getModById(@NotNull String modId);

    @NotNull
    default <T> List<T> mapLoadedMods(@NotNull Function<BlueberryMod, T> mapFunction) {
        List<T> list = new ArrayList<>();
        getLoadedMods().forEach(mod -> list.add(mapFunction.apply(mod)));
        return list;
    }

    @NotNull
    default List<BlueberryMod> getActiveMods() {
        List<BlueberryMod> mods = new ArrayList<>();
        getLoadedMods().forEach(mod -> {
            if (mod.getStateList().getCurrentState() != ModState.UNLOADED) mods.add(mod);
        });
        return ImmutableList.copyOf(mods);
    }

    @NotNull
    default <T> List<T> mapActiveMods(@NotNull Function<BlueberryMod, T> mapFunction) {
        List<T> list = new ArrayList<>();
        getActiveMods().forEach(mod -> list.add(mapFunction.apply(mod)));
        return list;
    }

    /**
     * Try to find mods in the current directory and loads all loadable mods.
     */
    void loadMods();

    /**
     * Returns the current config directory.
     * @return the config directory
     */
    @NotNull
    File getConfigDir();

    /**
     * Returns the current mods directory.
     * @return the mods directory
     */
    @NotNull
    File getModsDir();

    /**
     * Preload a mod from specific file (loads mod.yml).
     * You have to call this method before {@link #loadMod(File)} or it does not work.
     * @param file the file
     * @throws InvalidModDescriptionException if mod.yml contains an error
     * @throws ModDescriptionNotFoundException if mod.yml could not be found
     */
    @NotNull
    ModDescriptionFile preloadMod(@NotNull File file) throws InvalidModDescriptionException;

    /**
     * Load a mod from specific file.
     * @param file the file
     * @return the loaded mod
     * @throws InvalidModException if this file is an invalid mod
     */
    @NotNull
    BlueberryMod loadMod(@NotNull File file) throws InvalidModException;

    /**
     * Try to enable a mod that was disabled. This method is typically used to re-enable that was disabled
     * previously.
     * @param mod the mod to enable
     * @throws IllegalArgumentException if the mod is already enabled or classloader is already closed
     */
    void enableMod(@NotNull BlueberryMod mod) throws IllegalArgumentException;

    /**
     * Try to disable a mod.
     * @param mod the mod to disable
     * @throws IllegalArgumentException if mod is null or already disabled
     */
    void disableMod(@NotNull BlueberryMod mod);

    /**
     * Unregister a mod, and closes classloader if necessary. After classloader is closed, you will not be able to
     * re-enable the mod again.
     * @param mod the mod to unregister
     * @param unregister whether to unregister a mod or not (you will not be able to re-enable the mod again if you do)
     *                   LoadingOverlay will be triggered when unregistering ResourceManager!
     * @throws IllegalArgumentException if mod is null or already disabled
     */
    void disableMod(@NotNull BlueberryMod mod, boolean unregister);

    /**
     * Read mod description file from the file.
     * @param file the file
     * @return the mod description file
     * @throws InvalidModDescriptionException if mod description file is corrupt
     */
    @NotNull
    ModDescriptionFile getModDescription(@NotNull File file) throws InvalidModDescriptionException;

    /**
     * Try to register a mod.
     * @deprecated unstable API
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    BlueberryMod forceRegisterMod(@NotNull ModDescriptionFile description, boolean useModClassLoader) throws InvalidModException;

    void initModResources(@NotNull BlueberryMod mod);

    void callPreInit();

    void callInit();

    void callPostInit();

    @Nullable
    default InputStream getResourceAsStream(@NotNull String name) {
        InputStream in = null;
        for (BlueberryMod mod : this.getLoadedMods()) {
            ClassLoader cl = mod.getClassLoader();
            if ((in = cl.getResourceAsStream(name)) != null) break;
            if ((in = mod.getClass().getResourceAsStream(name)) != null) break;
        }
        return in;
    }
}
