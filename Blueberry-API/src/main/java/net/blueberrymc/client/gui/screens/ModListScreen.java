package net.blueberrymc.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import javax.annotation.Nullable;
import java.util.List;

public class ModListScreen extends Screen {
    private ModsList modsList;
    private final Screen previousScreen;
    private Button unloadButton;
    private Button configButton;

    public ModListScreen(Screen screen) {
        super(new BlueberryText("blueberry", "gui.screens.mods"));
        this.previousScreen = screen;
    }

    protected void init() {
        this.modsList = new ModsList(this.minecraft);
        this.children.add(this.modsList);
        this.addButton(new Button(this.width / 2 - 100, this.height - 38, 98, 20, new BlueberryText("blueberry", "gui.screens.mods.refresh"), (button) -> {
            this.minecraft.setScreen(this.previousScreen);
            this.minecraft.setScreen(new ModListScreen(this.previousScreen));
        }));
        this.addButton(new Button(this.width / 2 + 2, this.height - 38, 98, 20, CommonComponents.GUI_DONE, (button) -> this.minecraft.setScreen(this.previousScreen)));
        (this.unloadButton = this.addButton(new Button(10, this.height - 56, this.width / 5 - 20, 20, new BlueberryText("blueberry", "gui.screens.mods.disable"), button -> {
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
        (this.configButton = this.addButton(new Button(10, this.height - 34, this.width / 5 - 20, 20, new BlueberryText("blueberry", "gui.screens.mods.config"), button -> {
            ModsList.Entry entry = this.modsList.getSelected();
            if (entry != null && entry.mod.getVisualConfig().isNotEmpty()) {
                this.minecraft.setScreen(new ModConfigScreen(entry.mod.getVisualConfig(), this));
            }
        }))).active = false;
        super.init();
    }

    public void render(PoseStack poseStack, int i, int i2, float f) {
        this.modsList.render(poseStack, i, i2, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
        ModsList.Entry entry = this.modsList.getSelected();
        if (entry != null) {
            BlueberryMod mod = entry.mod;
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
            String authors = mod.getDescription().getAuthors();
            if (authors != null) {
                drawString(poseStack, this.font, "Authors: " + authors, this.width / 4, y += 10, 16777215);
            }
            String credits = mod.getDescription().getCredits();
            if (credits != null) {
                drawString(poseStack, this.font, "Credits: " + credits, this.width / 4, y += 10, 16777215);
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
        public ModsList(Minecraft minecraft) {
            super(minecraft, ModListScreen.this.width / 5, ModListScreen.this.height, 32, ModListScreen.this.height - 65 + 4, 18);

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

        protected void renderBackground(PoseStack poseStack) {
            ModListScreen.this.renderBackground(poseStack);
        }

        protected boolean isFocused() {
            return ModListScreen.this.getFocused() == this;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final BlueberryMod mod;

            public Entry(BlueberryMod mod) {
                this.mod = mod;
            }

            public void render(PoseStack poseStack, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean flag, float f) {
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
        }
    }

    public static void switchToModListScreen() {
        Screen previous = Minecraft.getInstance().screen;
        Minecraft.getInstance().setScreen(new ModListScreen(previous));
    }
}