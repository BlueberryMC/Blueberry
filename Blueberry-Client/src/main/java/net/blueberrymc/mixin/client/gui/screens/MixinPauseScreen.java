package net.blueberrymc.mixin.client.gui.screens;

import net.blueberrymc.client.gui.screens.ModListScreen;
import net.blueberrymc.common.resources.BlueberryText;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PauseScreen.class, remap = true)
public abstract class MixinPauseScreen extends Screen {
    protected MixinPauseScreen(Component component) {
        super(component);
    }

    @Inject(at = @At("TAIL"), method = "createPauseMenu")
    public void createPauseMenu(CallbackInfo ci) {
        addRenderableWidget(
                Button.builder(BlueberryText.text("blueberry", "gui.screens.mods"), btn -> ModListScreen.switchToModListScreen())
                        .bounds(this.width / 2 - 102, this.height / 4 + 144 - 16, 204, 20)
                        .build()
        );
    }
}
