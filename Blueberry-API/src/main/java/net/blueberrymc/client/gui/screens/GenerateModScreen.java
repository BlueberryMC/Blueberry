package net.blueberrymc.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blueberrymc.common.resources.BlueberryText;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenerateModScreen extends BlueberryScreen {
    private final Screen previousScreen;

    public GenerateModScreen(@Nullable Screen previousScreen) {
        super(BlueberryText.text("blueberry", "gui.screens.generate_mod.title"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        assert this.minecraft != null;
        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(previousScreen))
                        .bounds(this.width / 2 - 50, this.height - 38, 96, 20)
                        .build());
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float deltaFrameTime) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, deltaFrameTime);
    }
}
