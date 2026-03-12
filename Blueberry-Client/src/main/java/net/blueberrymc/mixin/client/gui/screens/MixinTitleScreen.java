package net.blueberrymc.mixin.client.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import net.blueberrymc.client.gui.screens.ModListScreen;
import net.blueberrymc.common.resources.BlueberryText;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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
    public void placeButton(int topPos, int spacing, CallbackInfoReturnable<Integer> ci) {
        Component multiplayerDisabledReason = getMultiplayerDisabledReason();
        Tooltip tooltip = multiplayerDisabledReason != null ? Tooltip.create(multiplayerDisabledReason) : null;
        ((MixinScreenAccessor) this).getRenderables().removeLast();
        this.addRenderableWidget(Button.builder(BlueberryText.text("blueberry", "gui.screens.mods"), (button) -> ModListScreen.switchToModListScreen()).bounds(this.width / 2 - 100, topPos + spacing * 2, 98, 20).tooltip(tooltip).build());
        (this.addRenderableWidget(Button.builder(Component.translatable("menu.online"), (button) -> this.minecraft.setScreen(new RealmsMainScreen(this))).bounds(this.width / 2 + 2, topPos + spacing * 2, 98, 20).build())).active = multiplayerDisabledReason == null;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"), method = "extractRenderState", locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderVersion(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci, float widgetFade, String versionString) {
        graphics.text(this.font, "Blueberry " + net.blueberrymc.common.Blueberry.getVersion().getFullyQualifiedVersion(), 2, this.height - 30, ARGB.white(widgetFade));
        graphics.text(this.font, net.blueberrymc.common.Blueberry.getModLoader().getLoadedMods().size() + " mods loaded, " + net.blueberrymc.common.Blueberry.getModLoader().getActiveMods().size() + " mods active", 2, this.height - 20, ARGB.white(widgetFade));
    }
}
