package net.blueberrymc.client.gui.screens;

import com.google.common.base.Joiner;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.BlueberryModLoader;
import net.blueberrymc.common.bml.InternalBlueberryModConfig;
import net.blueberrymc.common.bml.SimpleModInfo;
import net.blueberrymc.common.bml.client.gui.screens.ModLoadingProblemScreen;
import net.blueberrymc.common.bml.event.EventManager;
import net.blueberrymc.common.bml.loading.ModLoadingError;
import net.blueberrymc.common.bml.loading.ModLoadingErrors;
import net.blueberrymc.common.event.mod.ModReloadEvent;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.common.util.ReflectionHelper;
import net.blueberrymc.config.ModDescriptionFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Mod list screen, accessible via main menu.
 */
public class ModListScreen extends BlueberryScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private ModsList modsList;
    private final Screen previousScreen;
    private Button reloadButton;
    private Button recompileButton;
    private Button disableButton;
    private Button unloadButton;
    private Button configButton;

    public ModListScreen(@Nullable Screen screen) {
        super(BlueberryText.text("blueberry", "gui.screens.mods"));
        this.previousScreen = screen;
    }

    // TODO: clean-up
    protected void init() {
        assert this.minecraft != null;
        this.modsList = new ModsList(this.minecraft);
        this.children().add(this.modsList);
        this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods.refresh"), (button) -> {
            this.minecraft.setScreen(this.previousScreen);
            this.minecraft.setScreen(new ModListScreen(this.previousScreen));
        }).bounds(this.width / 2 - 150, this.height - 38, 96, 20).build());
        this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods.load"), (button) ->
                this.minecraft.setScreen(FileDialogScreen.create(
                        this,
                        FileDialogScreenOptions
                                .builder()
                                //.boundary(Blueberry.getGameDir()) // we probably don't need boundary
                                .fileType(FileDialogScreenOptions.FileType.ALL)
                                .initialDirectory(Blueberry.getModsDir())
                                .title(BlueberryText.text("blueberry", "gui.screens.mods.load.title"))
                                .callback(file -> {
                                    if (file != null) {
                                        File actualFileToLoad = file;
                                        if (actualFileToLoad.getName().equals("mod.yml")) {
                                            actualFileToLoad = file.getParentFile();
                                        }
                                        LOGGER.info("Trying to load mod from: {} (original path: {})", actualFileToLoad, file);
                                        tryLoadMod(actualFileToLoad);
                                        if (ModLoadingErrors.hasErrorOrWarning()) {
                                            this.minecraft.setScreen(new ModLoadingProblemScreen(this));
                                        }
                                    }
                                })
                                .build()))
        ).bounds(this.width / 2 - 50, this.height - 38, 96, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.minecraft.setScreen(this.previousScreen)).bounds(this.width / 2 + 50, this.height - 38, 96, 20).build());
        (this.reloadButton = this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods.reload"), button -> {
            try {
                ModsList.Entry entry = this.modsList.getSelected();
                if (entry != null) {
                    if (new ModReloadEvent(null, entry.mod).callEvent()) {
                        LOGGER.info("Reloading mod: {} ({})", entry.mod.name(), entry.mod.modId());
                        try {
                            if (entry.mod.onReload()) {
                                this.minecraft.reloadResourcePacks().thenAccept(v -> this.minecraft.setScreen(new ModListScreen(this.previousScreen)));
                            }
                        } catch (RuntimeException ex) {
                            LOGGER.error("Failed to reload mod", ex);
                            ModLoadingErrors.add(new ModLoadingError(entry.mod, "Failed to reload mod: " + ex.getMessage(), false));
                        }
                    }
                }
            } finally {
                if (ModLoadingErrors.hasErrorOrWarning()) {
                    ModLoadingErrors.add(new ModLoadingError(null, "One or more warnings/errors were detected. It is recommended to restart your Minecraft to prevent further issues.", true));
                    this.minecraft.setScreen(new ModLoadingProblemScreen(this));
                }
            }
        }).bounds(10, this.height - 78, this.width / 5 / 2 - 11, 20).build())).active = false;
        (this.recompileButton = this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods.recompile"), button -> {
            try {
                ModsList.Entry entry = this.modsList.getSelected();
                if (entry != null) {
                    if (entry.mod.isFromSource()) {
                        File file = entry.mod.getSourceDir();
                        if (file == null) throw new AssertionError("Source dir should not be null");
                        Blueberry.getModLoader().disableMod(entry.mod, true);
                        tryLoadMod(file);
                    }
                }
            } finally {
                if (ModLoadingErrors.hasErrorOrWarning()) {
                    ModLoadingErrors.add(new ModLoadingError(null, "One or more warning/error was detected. It is recommended to restart your Minecraft to prevent further issues.", true));
                    this.minecraft.setScreen(new ModLoadingProblemScreen(this));
                }
            }
        }).bounds(2 + this.width / 5 / 2, this.height - 78, this.width / 5 / 2 - 11, 20).build())).active = false;
        boolean isUnloadButtonEnabled = InternalBlueberryModConfig.Debug.allowUnload;
        int disableButtonWidth = isUnloadButtonEnabled ? (this.width / 5 / 2 - 11) : (this.width / 5 - 20);
        (this.disableButton = this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods.disable"), button -> {
            ModsList.Entry entry = this.modsList.getSelected();
            if (entry != null) {
                if (entry.mod.isUnloaded()) {
                    Blueberry.getModLoader().enableMod(entry.mod);
                    this.disableButton.setMessage(BlueberryText.text("blueberry", "gui.screens.mods.disable"));
                } else {
                    Blueberry.getModLoader().disableMod(entry.mod);
                    this.disableButton.setMessage(BlueberryText.text("blueberry", "gui.screens.mods.enable"));
                }
            }
        }).bounds(10, this.height - 56, disableButtonWidth, 20).build())).active = false;
        (this.unloadButton = this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods.unload"), button -> {
            ModsList.Entry entry = this.modsList.getSelected();
            if (entry != null) {
                Blueberry.getModLoader().disableMod(entry.mod, true);
                this.minecraft.reloadResourcePacks().thenAccept(v -> this.minecraft.setScreen(this));
                if (ModLoadingErrors.hasErrors()) {
                    ModLoadingErrors.add(new ModLoadingError(null, "One or more errors were detected. It is recommended to restart your Minecraft to prevent further issues.", true));
                    this.minecraft.setScreen(new ModLoadingProblemScreen(this));
                }
            }
        }).bounds(2 + this.width / 5 / 2, this.height - 56, this.width / 5 / 2 - 11, 20).build())).active = false;
        this.unloadButton.visible = isUnloadButtonEnabled;
        (this.configButton = this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods.config"), button -> {
            ModsList.Entry entry = this.modsList.getSelected();
            if (entry != null && entry.mod.getVisualConfig().isNotEmpty()) {
                this.minecraft.setScreen(new ModConfigScreen(entry.mod.getVisualConfig(), this));
            }
        }).bounds(10, this.height - 34, this.width / 5 / 2 - 11, 20).build())).active = false;
        this.addRenderableWidget(
                Button.builder(BlueberryText.text("blueberry", "gui.screens.mods.generate"), button -> this.minecraft.setScreen(new GenerateModScreen(this)))
                        .tooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mods.generate.tooltip")))
                        .bounds(2 + this.width / 5 / 2, this.height - 34, this.width / 5 / 2 - 11, 20)
                        .build());
        super.init();
        updateTooltip();
    }

    private void updateTooltip() {
        ModsList.Entry entry = this.modsList.getSelected();
        if (entry == null) {
            return;
        }
        assert this.minecraft != null;
        if (this.minecraft.level != null) {
            this.unloadButton.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mods.unload.in_world_tooltip")));
        }
        if (entry.mod.isFromSource()) {
            if (this.minecraft.level != null) {
                this.recompileButton.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mods.recompile.in_world_tooltip")));
            } else {
                this.recompileButton.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mods.recompile.recompile_tooltip")));
            }
        } else {
            this.recompileButton.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mods.recompile.unsupported")));
        }
        if (isReloadSupported(entry.mod)) {
            this.reloadButton.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mods.reload.reload_tooltip")));
        } else {
            this.reloadButton.setTooltip(Tooltip.create(BlueberryText.text("blueberry", "gui.screens.mods.reload.unsupported")));
        }
    }

    private void tryLoadMod(File file) {
        assert this.minecraft != null;
        try {
            ModDescriptionFile description;
            File compiled = null;
            try {
                Map.Entry<ModDescriptionFile, File> e = ((BlueberryModLoader) Blueberry.getModLoader()).preprocess(file);
                description = e.getKey();
                if (e.getKey().isSource() && e.getValue() != null) {
                    compiled = e.getValue();
                    ((BlueberryModLoader) Blueberry.getModLoader()).preprocess(compiled);
                }
            } catch (Throwable throwable) {
                LOGGER.error("Error during preprocessing {} (loaded from: {})", file.getName(), file.getAbsolutePath(), throwable);
                ModLoadingErrors.add(new ModLoadingError(new SimpleModInfo(file.getName(), file.getName()), "Error during preprocessing: " + throwable.getMessage(), false));
                return;
            }
            try {
                BlueberryMod mod = ((BlueberryModLoader) Blueberry.getModLoader()).loadMod(compiled != null ? compiled : file, compiled == null ? null : file);
                Blueberry.getModLoader().initModResources(mod);
                Blueberry.getModLoader().enableMod(mod);
            } catch (Exception ex) {
                LOGGER.error("Could not load a mod", ex);
                ModLoadingErrors.add(new ModLoadingError(description, "Could not load a mod: " + ex.getMessage(), false));
                return;
            }
            this.minecraft.reloadResourcePacks().thenAccept(v -> this.minecraft.setScreen(new ModListScreen(this.previousScreen)));
        } catch (Exception ex) {
            LOGGER.error("Could not load a mod", ex);
            ModLoadingErrors.add(new ModLoadingError(null, "Could not load a mod: " + ex.getMessage(), false));
        }
    }

    private static boolean isReloadSupported(@NotNull BlueberryMod mod) {
        Method method = ReflectionHelper.findMethodRecursively(mod.getClass(), "onReload");
        if (method != null
                && method.getReturnType().equals(boolean.class)
                && !method.getDeclaringClass().equals(BlueberryMod.class)) return true;
        return !EventManager.getHandlerList(ModReloadEvent.class).isEmpty();
    }

    private static final Joiner JOINER = Joiner.on(", ");

    public void render(@NotNull GuiGraphics guiGraphics , int i, int i2, float f) {
        this.modsList.render(guiGraphics, i, i2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
        ModsList.Entry entry = this.modsList.getSelected();
        if (entry != null) {
            BlueberryMod mod = entry.mod;
            this.reloadButton.active = isReloadSupported(mod);
            assert this.minecraft != null;
            this.recompileButton.active = this.minecraft.level == null && mod.isFromSource();
            this.disableButton.active = this.minecraft.level == null && mod.getDescription().isUnloadable();
            // TODO: disallow unload if mod is required by another mod or mixin is applied;
            this.unloadButton.active = this.minecraft.level == null && mod.getDescription().isUnloadable();
            this.configButton.active = mod.getVisualConfig().isNotEmpty();
            if (mod.isUnloaded()) {
                this.disableButton.setMessage(BlueberryText.text("blueberry", "gui.screens.mods.enable"));
            } else {
                this.disableButton.setMessage(BlueberryText.text("blueberry", "gui.screens.mods.disable"));
            }
            int y = 40;
            guiGraphics.drawString(this.font, "Mod Name: " + mod.name(), this.width / 4, y, 16777215);
            guiGraphics.drawString(this.font, "Mod ID: " + mod.getDescription().modId(), this.width / 4, y += 10, 16777215);
            guiGraphics.drawString(this.font, "Version: " + mod.getDescription().getVersion(), this.width / 4, y += 10, 16777215);
            List<String> authors = mod.getDescription().getAuthors();
            if (authors != null) {
                guiGraphics.drawString(this.font, "Authors: " + JOINER.join(authors), this.width / 4, y += 10, 16777215);
            }
            List<String> credits = mod.getDescription().getCredits();
            if (credits != null) {
                guiGraphics.drawString(this.font, "Credits: " + JOINER.join(credits), this.width / 4, y += 10, 16777215);
            }
            guiGraphics.drawString(this.font, "Status: " + mod.getStateList().getCurrentState().getName(), this.width / 4, y += 10, 16777215);
            List<String> description = mod.getDescription().getDescription();
            if (description != null) {
                y += 10;
                for (String s : description.stream().flatMap(s -> Arrays.stream(s.split("\\n"))).toList()) {
                    guiGraphics.drawString(this.font, s, this.width / 4, y += 10, 16777215);
                }
            }
        }
        super.render(guiGraphics, i, i2, f);
    }

    class ModsList extends ObjectSelectionList<ModsList.Entry> {
        public ModsList(@NotNull Minecraft minecraft) {
            super(minecraft, ModListScreen.this.width / 5, ModListScreen.this.height, 32, ModListScreen.this.height - 87 + 4, 18);

            for(BlueberryMod mod : Blueberry.getModLoader().getLoadedMods()) {
                Entry entry = new Entry(mod);
                this.addEntry(entry);
            }
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        public int getRowWidth() {
            return super.getRowWidth() - 30;
        }

        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            updateTooltip();
        }

        protected void renderBackground(@NotNull GuiGraphics guiGraphics) {
            ModListScreen.this.renderBackground(guiGraphics);
        }

        public boolean isFocused() {
            return ModListScreen.this.getFocused() == this;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final BlueberryMod mod;

            public Entry(@NotNull BlueberryMod mod) {
                this.mod = mod;
            }

            public void render(@NotNull GuiGraphics guiGraphics, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean flag, float f) {
                String s = this.mod.name();
                guiGraphics.drawString(ModListScreen.this.font, s, ModsList.this.width / 2 - ModListScreen.this.font.width(s) / 2, i2 + 2, 16777215, true);
            }

            public boolean mouseClicked(double d, double d2, int i) {
                if (i == 0) {
                    this.select();
                    return true;
                } else {
                    return false;
                }
            }

            private void select() {
                ModsList.this.setSelected(this);
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.translatable("narrator.select", this.mod.name());
            }
        }
    }

    public static void switchToModListScreen() {
        Screen previous = Minecraft.getInstance().screen;
        Minecraft.getInstance().setScreen(new ModListScreen(previous));
    }
}
