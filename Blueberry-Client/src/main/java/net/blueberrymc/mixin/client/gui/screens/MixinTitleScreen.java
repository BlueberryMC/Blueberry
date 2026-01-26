package net.blueberrymc.mixin.client.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import net.blueberrymc.client.gui.screens.ModListScreen;
import net.blueberrymc.common.resources.BlueberryText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = TitleScreen.class, priority = 900)
public abstract class MixinTitleScreen extends Screen {
    @Shadow
    @Nullable
    protected abstract Component getMultiplayerDisabledReason();

    protected MixinTitleScreen(Component component) {
        super(component);
    }

    @Inject(at = @At("TAIL"), method = "createNormalMenuOptions")
    public void placeButton(int i, int j, CallbackInfo ci) {
        assert minecraft != null;
        Component multiplayerDisabledReason = getMultiplayerDisabledReason();
        Tooltip tooltip = multiplayerDisabledReason != null ? Tooltip.create(multiplayerDisabledReason) : null;
        ((MixinScreenAccessor) this).getRenderables().removeLast();
        this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods"), (button) -> ModListScreen.switchToModListScreen()).bounds(this.width / 2 - 100, i + j * 2, 98, 20).tooltip(tooltip).build());
        (this.addRenderableWidget(Button.builder(Component.translatable("menu.online"), (button) -> this.minecraft.setScreen(new RealmsMainScreen(this))).bounds(this.width / 2 + 2, i + j * 2, 98, 20).build())).active = multiplayerDisabledReason == null;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"), method = "render", locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderVersion(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, float g, int k, String string) {
        guiGraphics.drawString(this.font, "Blueberry " + net.blueberrymc.common.Blueberry.getVersion().getFullyQualifiedVersion(), 2, this.height - 30, 0xFFFFFF | k);
        guiGraphics.drawString(this.font, net.blueberrymc.common.Blueberry.getModLoader().getLoadedMods().size() + " mods loaded, " + net.blueberrymc.common.Blueberry.getModLoader().getActiveMods().size() + " mods active", 2, this.height - 20, 0xFFFFFF | k);
    }
}
