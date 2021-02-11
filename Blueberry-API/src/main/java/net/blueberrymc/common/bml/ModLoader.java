package net.blueberrymc.common.bml;

import com.google.common.collect.ImmutableList;
import net.blueberrymc.config.ModDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface ModLoader {
    /**
     * Returns the list of registered mods.
     * @return mods list
     */
    @NotNull
    List<BlueberryMod> getLoadedMods();

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
     * Load a mod from specific file.
     * @param file the file
     * @return the loaded mod
     * @throws InvalidModException if this file is an invalid mod
     */
    @NotNull
    BlueberryMod loadMod(@NotNull File file) throws InvalidModException;

    /**
     * Try to enable a mod that was been disabled.
     * @param mod the mod to enable
     */
    void enableMod(@NotNull BlueberryMod mod);

    /**
     * Try to disable a mod.
     * @param mod the mod to disable
     */
    void disableMod(@NotNull BlueberryMod mod);

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
    @NotNull
    @Deprecated
    <T extends BlueberryMod> T forceRegisterMod(@NotNull ModDescriptionFile description, @NotNull Class<T> clazz) throws InvalidModException;

    void callPreInit();

    void callInit();

    void callPostInit();

    @Nullable
    default InputStream getResourceAsStream(@NotNull String name) {
        InputStream in = null;
        for (BlueberryMod mod : this.getLoadedMods()) {
            ModClassLoader cl = mod.getClassLoader();
            if ((in = cl.getResourceAsStream(name)) != null) break;
            if ((in = mod.getClass().getResourceAsStream(name)) != null) break;
        }
        return in;
    }
}
