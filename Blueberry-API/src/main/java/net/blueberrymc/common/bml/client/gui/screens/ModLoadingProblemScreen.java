package net.blueberrymc.common.bml.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.loading.ModLoadingError;
import net.blueberrymc.common.bml.loading.ModLoadingErrors;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class ModLoadingProblemScreen extends Screen {
    private ProblemList problemList;
    private Screen screen;

    public ModLoadingProblemScreen(@NotNull Screen screen) {
        super(new BlueberryText("blueberry", "gui.screens.mod_loading_problem.title").withStyle(ModLoadingErrors.hasErrors() ? ChatFormatting.RED : ChatFormatting.YELLOW));
        this.screen = screen;
    }

    public void refresh() {
        if (ModLoadingErrors.getErrors().isEmpty()) {
            if (this.screen instanceof TitleScreen) this.screen = new TitleScreen(true);
            Minecraft.getInstance().setScreen(this.screen);
        } else {
            Minecraft.getInstance().setScreen(new ModLoadingProblemScreen(this.screen));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        this.problemList = new ProblemList(this.minecraft);
        this.children.add(this.problemList);
        this.addButton(new Button(this.width / 2 - 100, this.height - 38, 98, 20, new BlueberryText("blueberry", "gui.screens.mod_loading_problem.open_log_file"), (button) -> Util.getPlatform().openFile(Blueberry.getLogFile())));
        this.addButton(new Button(this.width / 2 + 2, this.height - 38, 98, 20, CommonComponents.GUI_DONE, (button) -> {
            ModLoadingErrors.clear();
            Objects.requireNonNull(this.minecraft).setScreen(screen);
        }));
        super.init();
    }

    public void render(@NotNull PoseStack poseStack, int i, int i2, float f) {
        this.problemList.render(poseStack, i, i2, f);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
        super.render(poseStack, i, i2, f);
    }

    class ProblemList extends ObjectSelectionList<ProblemList.Entry> {
        public ProblemList(@NotNull Minecraft minecraft) {
            super(minecraft, ModLoadingProblemScreen.this.width, ModLoadingProblemScreen.this.height, 32, ModLoadingProblemScreen.this.height - 65 + 4, 18);

            for (ModLoadingError error : ModLoadingErrors.getErrors()) {
                this.addEntry(new Entry(error));
            }
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        public int getRowWidth() {
            return ModLoadingProblemScreen.this.width - 10;
        }

        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
        }

        protected void renderBackground(@NotNull PoseStack poseStack) {
            ModLoadingProblemScreen.this.renderBackground(poseStack);
        }

        protected boolean isFocused() {
            return ModLoadingProblemScreen.this.getFocused() == this;
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final ModLoadingError error;

            public Entry(@NotNull ModLoadingError error) {
                this.error = error;
            }

            public void render(@NotNull PoseStack poseStack, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean flag, float f) {
                String modName = this.error.modInfo != null ? this.error.modInfo.getName() + ": " : null;
                String s = modName + this.error.getMessage();
                ModLoadingProblemScreen.this.font.drawShadow(poseStack, s, (float)(ProblemList.this.width / 2 - ModLoadingProblemScreen.this.font.width(s) / 2), (float)(i2 + 2), this.error.isWarning ? 0xFFFF55 : 0xFF5555, true);
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
                ProblemList.this.setSelected(this);
            }
        }
    }
}