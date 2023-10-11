package net.blueberrymc.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A "backup" or "load anyway" screen.
 */
public class MultiLineBackupConfirmScreen extends Screen {
    @Nullable
    private final Screen lastScreen;
    protected final Listener listener;
    private final Component description;
    private final boolean promptForCacheErase;
    private List<MultiLineLabel> message = Collections.singletonList(MultiLineLabel.EMPTY);
    protected int id;
    private Checkbox eraseCache;
    private final List<Component> lines;

    public MultiLineBackupConfirmScreen(@Nullable Screen screen, @NotNull Listener listener, @NotNull Component title, @NotNull Component description, boolean promptForCacheErase, @NotNull List<Component> lines) {
        super(title);
        this.lastScreen = screen;
        this.listener = listener;
        this.description = description;
        this.promptForCacheErase = promptForCacheErase;
        this.lines = lines;
    }

    protected void init() {
        super.init();
        this.message = new ArrayList<>();
        this.message.add(MultiLineLabel.create(this.font, this.description, this.width - 50));
        this.message.add(MultiLineLabel.create(this.font, Component.literal(" "), 10));
        for (Component line : this.lines) {
            this.message.add(MultiLineLabel.create(this.font, line, this.width - 50));
        }
        int lineCount = this.message.stream().map(MultiLineLabel::getLineCount).reduce(Integer::sum).orElse(0);
        int y = 9;
        for (int i = 0; i < lineCount; i++) {
            if (y + 150 > this.height) break;
            y += 9;
        }
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.backupJoinConfirmButton"), (button) -> this.listener.proceed(true, this.eraseCache.selected())).bounds(this.width / 2 - 155, 100 + y, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.backupJoinSkipButton"), (button) -> this.listener.proceed(false, this.eraseCache.selected())).bounds(this.width / 2 - 155 + 160, 100 + y, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> Objects.requireNonNull(this.minecraft).setScreen(this.lastScreen)).bounds(this.width / 2 - 155 + 80, 124 + y, 150, 20).build());
        this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + y, 150, 20, Component.translatable("selectWorld.backupEraseCache"), false);
        if (this.promptForCacheErase) {
            this.addRenderableWidget(this.eraseCache);
        }

    }

    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaFrameTime) {
        this.renderBackground(guiGraphics, mouseX, mouseY, deltaFrameTime);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 16777215);
        int y = 70;
        for (MultiLineLabel label : this.message) {
            label.renderCentered(guiGraphics, this.width / 2, y);
            if (y + 150 > this.height) {
                guiGraphics.drawCenteredString(this.font, "...", this.width / 2, y + 9, 16777215);
                break;
            }
            y += label.getLineCount() * 9;
        }
        super.render(guiGraphics, mouseX, mouseY, deltaFrameTime);
    }

    @Override
    public void onClose() {
        Objects.requireNonNull(this.minecraft).setScreen(this.lastScreen);
    }

    public interface Listener {
        void proceed(boolean doBackup, boolean eraseCache);
    }
}
