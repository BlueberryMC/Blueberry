package net.blueberrymc.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.blueberrymc.common.util.FileUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class FileDialogScreen extends BlueberryScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Component description;
    private final Screen firstScreen;
    private final FileDialogScreenOptions options;
    private boolean calledCallback = false;
    private FileList fileList;
    private Button cancelButton;

    private FileDialogScreen(@NotNull Screen firstScreen, @NotNull FileDialogScreenOptions options) {
        super(options.title());
        this.description = generateTitle(options.getInitialDirectory());
        this.firstScreen = firstScreen;
        this.options = options;
    }

    @Override
    protected void init() {
        assert minecraft != null;
        fileList = new FileList(minecraft);
        children().add(fileList);
        // Cancel button
        addRenderableWidget(cancelButton = new Button(width / 2 - 100, height - 38, 200, 20, CommonComponents.GUI_CANCEL, (button) -> this.onClose()));
    }

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(firstScreen);
        invokeCallback(null);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float deltaFrameTime) {
        fileList.render(poseStack, mouseX, mouseY, deltaFrameTime);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 16777215);
        drawCenteredString(poseStack, this.font, this.description, this.width / 2, 32, 16777215);
        cancelButton.render(poseStack, mouseX, mouseY, deltaFrameTime);
    }

    public void invokeCallback(@Nullable File file) {
        if (calledCallback) return;
        calledCallback = true;
        if (file != null) {
            if (options.fileType() == FileDialogScreenOptions.FileType.DIRECTORY && !file.isDirectory()) {
                LOGGER.warn("Tried to invoke callback with a file that is not a directory");
                file = null;
            } else if (options.fileType() == FileDialogScreenOptions.FileType.FILE && !file.isFile()) {
                LOGGER.warn("Tried to invoke callback with a directory that is not a file");
                file = null;
            } else if (!options.filter().test(file)) {
                LOGGER.warn("Tried to invoke callback with a file that does not match the filter");
                file = null;
            }
        }
        options.runCallback(file);
    }

    @Contract("_, _, -> new")
    public static @NotNull FileDialogScreen create(@NotNull Screen firstScreen, @NotNull FileDialogScreenOptions options) {
        return new FileDialogScreen(firstScreen, options);
    }

    @NotNull
    private static Component generateTitle(@NotNull File file) {
        int visited = 0;
        String[] split = file.getAbsolutePath().split("[/\\\\]");
        StringBuilder path = new StringBuilder();
        // reverse for-loop
        for (int i = split.length - 1; i >= 0; i--) {
            if (split[i].isEmpty() || split[i].equals(".")) {
                continue;
            }
            path.insert(0, split[i] + "/");
            visited++;
            if (visited == 4) {
                if (split.length - i > 1) {
                    path.insert(0, ".../");
                }
                break;
            }
        }
        return Component.literal(path.toString());
    }

    class FileList extends ObjectSelectionList<FileList.Entry> {
        private static final Logger LOGGER = LogManager.getLogger();
        private final Button cdButton;
        private final Button selectButton;

        public FileList(@NotNull Minecraft minecraft) {
            super(minecraft, FileDialogScreen.this.width, FileDialogScreen.this.height, 52, FileDialogScreen.this.height - 50, 18);

            if (FileUtil.isRoot(options.getInitialDirectory()) && FileUtil.hasMultipleRoots()) {
                for (File root : File.listRoots()) {
                    if (root.equals(options.getInitialDirectory())) continue;
                    addEntry(new Entry("[ " + root + " ]", root));
                }
            }
            File parent = options.getInitialDirectory().getParentFile();
            if (parent != null && FileUtil.isFileInsideBoundary(options.boundary(), parent)) {
                addEntry(new Entry("../", parent));
            }
            File[] files = FileDialogScreen.this.options.getInitialDirectory().listFiles();
            if (files != null) {
                List<File> list = new ObjectArrayList<>(files);
                list.sort(Comparator.comparing(File::getName));
                Consumer<Boolean> addEntries = (Boolean directory) -> {
                    for (File file : files) {
                        if (file.isDirectory() != directory) {
                            continue;
                        }
                        if (options.fileType() == FileDialogScreenOptions.FileType.DIRECTORY && file.isFile()) {
                            continue;
                        }
                        if (options.filter().test(file)) {
                            FileList.Entry entry = new FileList.Entry(file);
                            this.addEntry(entry);
                        }
                    }
                };
                addEntries.accept(true);
                addEntries.accept(false);
            }
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
            cdButton = new Button(0, 0, 20, 20, Component.literal(">").withStyle(ChatFormatting.GOLD), (button) -> {
                File file = getSelected().file;
                if (!file.isDirectory()) {
                    LOGGER.warn("Trying to cd to a non-directory: {}", file);
                    return;
                }
                if (!FileUtil.isFileInsideBoundary(options.boundary(), file)) {
                    LOGGER.warn("Tried to cd to a directory outside of the boundary: {}", file);
                    return;
                }
                minecraft.setScreen(FileDialogScreen.create(firstScreen, options.toBuilder().initialDirectory(file).build()));
            });
            cdButton.visible = false;
            selectButton = new Button(0, 0, 20, 20, Component.literal("\u2714").withStyle(ChatFormatting.GREEN), (button) -> {
                minecraft.setScreen(firstScreen);
                options.runCallback(getSelected().file);
            });
            selectButton.visible = false;
            addRenderableWidget(cdButton);
            addRenderableWidget(selectButton);
        }

        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        public int getRowWidth() {
            return super.getRowWidth() - 30;
        }

        public void setSelected(@Nullable FileDialogScreen.FileList.Entry entry) {
            super.setSelected(entry);
        }

        protected void renderBackground(@NotNull PoseStack poseStack) {
            FileDialogScreen.this.renderBackground(poseStack);
        }

        protected boolean isFocused() {
            return FileDialogScreen.this.getFocused() == this;
        }

        @Override
        protected void renderList(@NotNull PoseStack poseStack, int rowLeft, int i2, int mouseX, int mouseY, float deltaFrameTime) {
            super.renderList(poseStack, rowLeft, i2, mouseX, mouseY, deltaFrameTime);
            int itemCount = this.getItemCount();

            for (int itemIndex = 0; itemIndex < itemCount; ++itemIndex) {
                int rowTop = this.getRowTop(itemIndex);
                int rowBottom = this.getRowTop(itemIndex) + this.itemHeight;
                if (rowBottom >= this.y0 && rowTop <= this.y1) {
                    Entry entry = this.getEntry(itemIndex);
                    int rowWidth = this.getRowWidth();
                    if (this.isSelectedItem(itemIndex)) {
                        if (entry.file.isDirectory()) {
                            cdButton.x = rowLeft + rowWidth;
                            cdButton.y = rowTop - 3;
                            cdButton.visible = true;
                            cdButton.render(poseStack, mouseX, mouseY, deltaFrameTime);
                            if (options.fileType() == FileDialogScreenOptions.FileType.DIRECTORY ||
                                    options.fileType() == FileDialogScreenOptions.FileType.ALL) {
                                selectButton.x = rowLeft + rowWidth + 22;
                                selectButton.y = rowTop - 3;
                                selectButton.visible = true;
                                selectButton.render(poseStack, mouseX, mouseY, deltaFrameTime);
                            } else {
                                selectButton.visible = false;
                            }
                        } else {
                            cdButton.visible = false;
                            selectButton.x = rowLeft + rowWidth;
                            selectButton.y = rowTop - 3;
                            selectButton.visible = true;
                            selectButton.render(poseStack, mouseX, mouseY, deltaFrameTime);
                        }
                    }
                }
            }
        }

        public class Entry extends ObjectSelectionList.Entry<FileList.Entry> {
            private final String name;
            private final File file;

            public Entry(@NotNull File file) {
                String s = file.getName();
                if (file.isDirectory()) {
                    s += "/";
                }
                this.name = s;
                this.file = file;
            }

            public Entry(@NotNull String name, @NotNull File file) {
                this.name = name;
                this.file = file;
            }

            public void render(@NotNull PoseStack poseStack, int i, int i2, int i3, int i4, int i5, int i6, int i7, boolean flag, float f) {
                FileDialogScreen.this.font.drawShadow(poseStack, name, (float)(FileList.this.width / 2 - FileDialogScreen.this.font.width(name) / 2), (float)(i2 + 2), 16777215, false);
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
                FileList.this.setSelected(this);
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.translatable("narrator.select", this.file.getName());
            }
        }
    }
}
