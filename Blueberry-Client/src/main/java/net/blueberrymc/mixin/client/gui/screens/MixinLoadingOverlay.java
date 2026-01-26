package net.blueberrymc.mixin.client.gui.screens;

import net.blueberrymc.client.EarlyLoadingScreen;
import net.blueberrymc.common.Blueberry;
import net.blueberrymc.common.bml.client.gui.screens.ModLoadingProblemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LoadingOverlay.class, remap = true)
public class MixinLoadingOverlay {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadInstance;getActualProgress()F"), method = "render")
    public void renderMessages(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        EarlyLoadingScreen.getInstance().renderMessagesFromGUI(guiGraphics);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"), method = "render")
    public void callPostInit(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        Blueberry.getModLoader().callPostInit();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(Lnet/minecraft/client/Minecraft;II)V"), method = "render")
    public void checkModLoadingProblemScreen(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        if (this.minecraft.screen instanceof ModLoadingProblemScreen) {
            ((ModLoadingProblemScreen) this.minecraft.screen).refresh();
        }
    }
}
