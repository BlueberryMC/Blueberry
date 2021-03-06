package net.blueberrymc.client.gui.screens;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.BlueberryModLoader;
import net.blueberrymc.common.bml.InvalidModException;
import net.blueberrymc.common.bml.SimpleModInfo;
import net.blueberrymc.common.bml.client.gui.screens.ModLoadingProblemScreen;
import net.blueberrymc.common.bml.loading.ModLoadingError;
import net.blueberrymc.common.bml.loading.ModLoadingErrors;
import net.blueberrymc.config.ModDescriptionFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ModListScreen extends BlueberryScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private ModsList modsList;
    private final Screen previousScreen;
    private Button reloadButton;
    private Button unloadButton;
    private Button configButton;

    public ModListScreen(@Nullable Screen screen) {
        super(new BlueberryText("blueberry", "gui.screens.mods"));
        this.previousScreen = screen;
    }

    protected void init() {
        this.modsList = new ModsList(this.minecraft);
        this.children().add(this.modsList);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 38, 98, 20, new BlueberryText("blueberry", "gui.screens.mods.refresh"), (button) -> {
            this.minecraft.setScreen(this.previousScreen);
            this.minecraft.setScreen(new ModListScreen(this.previousScreen));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 2, this.height - 38, 98, 20, CommonComponents.GUI_DONE, (button) -> this.minecraft.setScreen(this.previousScreen)));
        (this.reloadButton = this.addRenderableWidget(new Button(10, this.height - 78, this.width / 5 - 20, 20, new BlueberryText("blueberry", "gui.screens.mods.reload"), button -> {
            try {
                ModsList.Entry entry = this.modsList.getSelected();
                if (entry != null) {
                    File file = entry.mod.getSourceDir();
                    if (file == null) throw new AssertionError("Source dir should not be null");
                    Blueberry.getModLoader().disableMod(entry.mod, true);
                    ModDescriptionFile description;
                    File compiled = null;
                    try {
                        Map.Entry<ModDescriptionFile, File> e = ((BlueberryModLoader) Blueberry.getModLoader()).preprocess(file);
                        description = e.getKey();
                        if (e.getKey().isSource() && e.getValue() != null) {
                            compiled = e.getValue();
                        }
                    } catch (Throwable throwable) {
                        LOGGER.error("Error during preprocessing {} (loaded from: {})", file.getName(), file.getAbsolutePath(), throwable);
                        ModLoadingErrors.add(new ModLoadingError(new SimpleModInfo(file.getName(), file.getName()), "Error during preprocessing: " + throwable.getMessage(), false));
                        return;
                    }
                    BlueberryMod mod;
                    try {
                        mod = ((BlueberryModLoader) Blueberry.getModLoader()).loadMod(compiled != null ? compiled : file, compiled == null ? null : file);
                    } catch (InvalidModException ex) {
                        LOGGER.error("Could not load a mod: " + ex);
                        ModLoadingErrors.add(new ModLoadingError(description, "Could not load a mod: " + ex.getMessage(), false));
                        return;
                    }
                    Blueberry.getModLoader().initModResources(mod);
                    Blueberry.getModLoader().enableMod(mod);
                    this.minecraft.reloadResourcePacks().thenAccept(v -> {
                        this.minecraft.setScreen(this.previousScreen);
                        this.minecraft.setScreen(new ModListScreen(this.previousScreen));
                    });
                }
            } finally {
                if (ModLoadingErrors.hasErrors()) {
                    ModLoadingErrors.add(new ModLoadingError(null, "One or more warning/error was detected. It is recommended to restart your Minecraft to prevent further issues.", true));
                    this.minecraft.setScreen(new ModLoadingProblemScreen(this));
                }
            }
        }, (button, poseStack, i, i1) -> {
            ModsList.Entry entry = this.modsList.getSelected();
            if (entry != null) {
                if (entry.mod.isFromSource()) {
                    if (this.minecraft.level != null) {
                        renderTooltip(poseStack, new BlueberryText("blueberry", "gui.screens.mods.cannot_reload_level_tooltip"), i, i1);
                    }
                } else {
                    renderTooltip(poseStack, new BlueberryText("blueberry", "gui.screens.mods.cannot_reload_tooltip"), i, i1);
                }
            }
        }))).active = false;
        (this.unloadButton = this.addRenderableWidget(new Button(10, this.height - 56, this.width / 5 - 20, 20, new BlueberryText("blueberry", "gui.screens.mods.disable"), button -> {
            ModsList.Entry entry = this.modsList.getSelected();
            if (entry != null) {
                if (entry.mod.isUnloaded()) {
                    Blueberry.getModLoader().enableMod(entry.mod);
                    this.unloadButton.setMessage(new BlueberryText("blueberry", "gui.screens.mods.disable"));
                } else {
                    Blueberry.getModLoader().disableMod(entry.mod);
                    this.unloadButton.setMessage(new BlueberryText("blueberry", "gui.screens.mods.enable"));
                }
            }
        }))).active = false;
        (this.configButton = this.addRenderableWidget(new Button(10, this.height - 34, this.width / 5 - 20, 20, new BlueberryText("blueberry", "gui.screens.mods.config"), button -> {
            ModsList.Entry entry = this.modsList.getSelected();
            if (entry != null && entry.mod.getVisualConfig().isNotEmpty()) {
                this.minecraft.setScreen(new ModConfigScreen(entry.mod.getVisualConfig(), this));
            }
        }))).active = false;
        super.init();
    }

    private static final Joiner JOINER = Joiner.on(", ");

    public void render(@NotNull PoseStack poseStack, int i, int i2, float f) {
        this.modsList.render(poseStack, i, i2, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
        ModsList.Entry entry = this.modsList.getSelected();
        if (entry != null) {
            BlueberryMod mod = entry.mod;
            this.reloadButton.active = this.minecraft.level == null && mod.isFromSource();
            this.unloadButton.active = this.minecraft.level == null && mod.getDescription().isUnloadable();
            this.configButton.active = mod.getVisualConfig().isNotEmpty();
            if (mod.isUnloaded()) {
                this.unloadButton.setMessage(new BlueberryText("blueberry", "gui.screens.mods.enable"));
            } else {
                this.unloadButton.setMessage(new BlueberryText("blueberry", "gui.screens.mods.disable"));
            }
            int y = 40;
            drawString(poseStack, this.font, "Mod Name: " + mod.getName(), this.width / 4, y, 16777215);
            drawString(poseStack, this.font, "Mod ID: " + mod.getDescription().getModId(), this.width / 4, y += 10, 16777215);
            drawString(poseStack, this.font, "Version: " + mod.getDescription().getVersion(), this.width / 4, y += 10, 16777215);
            List<String> authors = mod.getDescription().getAuthors();
            if (authors != null) {
                drawString(poseStack, this.font, "Authors: " + JOINER.join(authors), this.width / 4, y += 10, 16777215);
            }
            List<String> credits = mod.getDescription().getCredits();
            if (credits != null) {
                drawString(poseStack, this.font, "Credits: " + JOINER.join(credits), this.width / 4, y += 10, 16777215);
            }
            drawString(poseStack, this.font, "Status: " + mod.getStateList().getCurrentState().getName(), this.width / 4, y += 10, 16777215);
            List<String> description = mod.getDescription().getDescription();
            if (description != null) {
                y += 10;
                for (String s : description) {
                    drawString(poseStack, this.font, s, this.width / 4, y += 10, 16777215);
                }
            }
        }
        super.render(poseStack, i, i2, f);
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
        }

        protected void renderBackground(@NotNull PoseStack poseStack) {
            ModListScreen.this.renderBackground(poseStack);
        }

        protected boolean isFocused() {
            return ModListScreen.this.getFocused() == this;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final BlueberryMod mod;

            public Entry(@NotNull BlueberryMod mod) {
                this.mod = mod;
            }

            public void render(@NotNull PoseStack poseStack, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean flag, float f) {
                String s = this.mod.getName();
                ModListScreen.this.font.drawShadow(poseStack, s, (float)(ModsList.this.width / 2 - ModListScreen.this.font.width(s) / 2), (float)(i2 + 2), 16777215, true);
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
                return new TranslatableComponent("narrator.select", this.mod.getName());
            }
        }
    }

    public static void switchToModListScreen() {
        Screen previous = Minecraft.getInstance().screen;
        Minecraft.getInstance().setScreen(new ModListScreen(previous));
    }
}