package net.blueberrymc.client.gui.screens;

import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.resources.BlueberryText;
import net.blueberrymc.config.ModDescriptionFile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GenerateModScreen extends BlueberryScreen {
    private final Screen previousScreen;
    private final List<Object> blockers = new ArrayList<>();
    private final List<Consumer<GuiGraphics>> callbacks = new ArrayList<>();
    private String modName = "";
    private String modId = "";
    private String targetDirectory = "";
    private Button generateButton;

    public GenerateModScreen(@Nullable Screen previousScreen) {
        super(BlueberryText.text("blueberry", "gui.screens.generate_mod.title"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        blockers.clear();
        assert this.minecraft != null;
        int offset = 38;
        int _maxWidth = 50;
        var allTexts = List.of(
                BlueberryText.text("blueberry", "gui.screens.generate_mod.mod_name"),
                BlueberryText.text("blueberry", "gui.screens.generate_mod.mod_id"),
                BlueberryText.text("blueberry", "gui.screens.generate_mod.target_directory")
        );
        for (var component : allTexts) {
            int textWidth = font.width(component);
            if (textWidth > _maxWidth) _maxWidth = textWidth;
        }
        final int maxWidth = _maxWidth;
        BiConsumer<Component, Integer> addLabel = (component, finalOffset) -> callbacks.add(guiGraphics -> {
            guiGraphics.drawString(font, component, this.width / 2 - this.width / 6 - maxWidth - 6, finalOffset + 6, 0xFFFFFF);
        });
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(previousScreen))
                        .bounds(this.width / 2 + 2, this.height - 38, 96, 20)
                        .build());
        generateButton = this.addRenderableWidget(Button
                .builder(
                        BlueberryText.text("blueberry", "gui.screens.generate_mod.generate"),
                        (button) -> {
                            // TODO
                        })
                .bounds(this.width / 2 - 98, this.height - 38, 96, 20)
                .build());
        addLabel.accept(allTexts.get(0), offset);
        var modNameEditBox = new EditBox(font, width / 2 - width / 6 + 6, offset, width / 3, 20, Component.literal("Mod name"));
        modNameEditBox.setMaxLength(255);
        modNameEditBox.setValue(modName);
        this.addRenderableWidget(modNameEditBox);
        offset += 24;
        addLabel.accept(allTexts.get(1), offset);
        var modIdEditBox = new EditBox(font, width / 2 - width / 6 + 6, offset, width / 3, 20, Component.literal("Mod name"));
        modIdEditBox.setMaxLength(255);
        modIdEditBox.setValue(modId);
        this.addRenderableWidget(modIdEditBox);
        offset += 24;
        addLabel.accept(allTexts.get(2), offset);
        var targetDirectoryEditBox = new EditBox(font, width / 2 - width / 6 + 30, offset, width / 3 - 24, 20, Component.literal("Target directory"));
        targetDirectoryEditBox.setMaxLength(2048);
        targetDirectoryEditBox.setValue(targetDirectory);
        targetDirectoryEditBox.setSuggestion(File.separator + modName);
        modNameEditBox.setResponder(it -> {
            modName = it;
            targetDirectoryEditBox.setSuggestion(File.separator + modName);
            checkTargetDirectory(targetDirectoryEditBox);
            String projectedModId = it.replace(' ', '-').toLowerCase(Locale.ROOT);
            if (!ModDescriptionFile.MOD_ID_PATTERN.matcher(projectedModId).matches()) {
                modIdEditBox.setValue(projectedModId);
                modIdEditBox.setTextColor(0xFF0000);
                block(modIdEditBox);
            } else {
                modIdEditBox.setTextColor(0xFFFFFF);
                modIdEditBox.setValue(projectedModId);
                modId = projectedModId;
                unblock(modIdEditBox);
            }
        });
        modIdEditBox.setResponder(it -> {
            if (!ModDescriptionFile.MOD_ID_PATTERN.matcher(it).matches()) {
                modIdEditBox.setTextColor(0xFF0000);
                block(modIdEditBox);
            } else {
                modIdEditBox.setTextColor(0xFFFFFF);
                modId = it;
                unblock(modIdEditBox);
            }
        });
        targetDirectoryEditBox.setResponder(it -> {
            targetDirectoryEditBox.setSuggestion(File.separator + modName);
            checkTargetDirectory(targetDirectoryEditBox);
        });
        this.addRenderableWidget(targetDirectoryEditBox);
        this.addRenderableWidget(
                Button.builder(Component.literal("..."), (button) ->
                                this.minecraft.setScreen(FileDialogScreen.create(
                                                this,
                                                FileDialogScreenOptions
                                                        .builder()
                                                        //.boundary(Blueberry.getGameDir()) // we probably don't need boundary
                                                        .fileType(FileDialogScreenOptions.FileType.DIRECTORY)
                                                        .initialDirectory(Blueberry.getModsDir())
                                                        .title(BlueberryText.text("blueberry", "gui.screens.generate_mod.select.title"))
                                                        .callback(file -> {
                                                            if (file != null) {
                                                                targetDirectory = file.getAbsolutePath();
                                                            }
                                                        })
                                                        .build()
                                        )
                                )
                        )
                        .bounds(this.width / 2 - this.width / 6 + 6, offset, 20, 20)
                        .build());
        checkTargetDirectory(targetDirectoryEditBox);
    }

    private void checkTargetDirectory(EditBox editBox) {
        if (editBox.getValue().isBlank()) {
            block(editBox);
            return;
        }
        if (modName.isBlank()) {
            editBox.setTextColor(0xFF0000);
            editBox.setTooltip(Tooltip.create(Component.literal("Mod Name is empty").withStyle(ChatFormatting.RED)));
            block(editBox);
            return;
        }
        try {
            File file = new File(editBox.getValue() + File.separator + modName);
            if (file.exists()) {
                editBox.setTextColor(0xFF0000);
                editBox.setTooltip(Tooltip.create(Component.literal("Directory already exists").withStyle(ChatFormatting.RED)));
                block(editBox);
                return;
            }
        } catch (Exception e) {
            editBox.setTextColor(0xFF0000);
            editBox.setTooltip(Tooltip.create(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED)));
            block(editBox);
            return;
        }
        targetDirectory = editBox.getValue();
        editBox.setTextColor(0xFFFFFF);
        editBox.setTooltip(null);
        unblock(editBox);
    }

    private void block(Object blocker) {
        if (!blockers.contains(blocker)) blockers.add(blocker);
        generateButton.active = false;
        generateButton.setTooltip(Tooltip.create(Component.literal("Blocked by:\n" + blockers.stream().map(o -> ((AbstractWidget) o).getMessage().getString()).collect(Collectors.joining("\n")))));
    }

    private void unblock(Object blocker) {
        blockers.remove(blocker);
        generateButton.active = blockers.isEmpty();
        if (blockers.isEmpty()) {
            generateButton.setTooltip(null);
        } else {
            generateButton.setTooltip(Tooltip.create(Component.literal("Blocked by:\n" + blockers.stream().map(o -> ((AbstractWidget) o).getMessage().getString()).collect(Collectors.joining("\n")))));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaFrameTime) {
        renderBackground(guiGraphics, mouseX, mouseY, deltaFrameTime);
        for (var callback : callbacks) callback.accept(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, deltaFrameTime);
    }
}
