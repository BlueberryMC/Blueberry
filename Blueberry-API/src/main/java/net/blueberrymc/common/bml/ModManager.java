package net.blueberrymc.common.bml;

import com.google.common.collect.ImmutableList;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.event.Event;
import net.blueberrymc.common.bml.event.EventManager;
import net.blueberrymc.common.bml.event.Listener;
import net.blueberrymc.network.mod.ModInfo;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModManager {
    @NotNull
    public EventManager getEventManager() {
        return Blueberry.getEventManager();
    }

    public void registerEvents(@NotNull BlueberryMod mod, @NotNull Listener listener) {
        getEventManager().registerEvents(mod, listener);
    }

    public void callEvent(@NotNull Event event) {
        getEventManager().callEvent(event);
    }

    public void loadMods() {
        Blueberry.getModLoader().loadMods();
    }

    @NotNull
    public BlueberryMod loadMod(@NotNull File file) {
        return Blueberry.getModLoader().loadMod(file);
    }

    public void unloadMod(@NotNull BlueberryMod mod) {
        Blueberry.getModLoader().disableMod(mod);
    }

    @NotNull
    public File getModsDir() {
        return Blueberry.getModLoader().getModsDir();
    }

    @NotNull
    public File getConfigDir() {
        return Blueberry.getModLoader().getConfigDir();
    }

    public void loadPacks(@NotNull Consumer<Pack> consumer, @NotNull Pack.PackConstructor packConstructor) {
        for (BlueberryMod mod : Blueberry.getModLoader().getLoadedMods()) {
            try {
                Pack pack = Pack.create(mod.getDescription().getModId(), true, () -> mod.getResourceManager().getPackResources(), packConstructor, Pack.Position.TOP, PackSource.BUILT_IN);
                if (pack != null) consumer.accept(pack);
            } catch (IllegalArgumentException ex) {
                break; // assume we're not finished resource manager load yet
            }
        }
    }

    @NotNull
    public List<ModInfo> getModInfos() {
        List<ModInfo> modInfos = new ArrayList<>();
        Blueberry.getModLoader().getLoadedMods().forEach(mod -> {
            modInfos.add(new ModInfo(mod.getDescription().getModId(), mod.getDescription().getVersion()));
        });
        return ImmutableList.copyOf(modInfos);
    }

    @Nullable
    public BlueberryMod getModById(@NotNull String modId) {
        return Blueberry.getModLoader().getModById(modId);
    }

    @Nullable
    public BlueberryMod getModByName(@NotNull String modName) {
        for (BlueberryMod mod : Blueberry.getModLoader().getLoadedMods()) {
            if (mod.getName().equals(modName)) return mod;
        }
        return null;
    }
}
