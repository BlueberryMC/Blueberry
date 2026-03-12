package net.blueberrymc.mixin.client.gui.components;

import net.blueberrymc.common.Blueberry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = DebugScreenOverlay.class, remap = true)
public abstract class MixinDebugScreenOverlay {
//    @Shadow
//    protected abstract void extractLines(GuiGraphicsExtractor guiGraphics, List<String> list, boolean bl);

    //@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;extractLines(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;Z)V"), method = "drawSystemInformation")
    //public void renderBlueberryLines(DebugScreenOverlay instance, GuiGraphicsExtractor guiGraphics, List<String> list, boolean bl) {

    @Inject(at = @At("HEAD"), method = "extractLines")
    public void renderBlueberryLines(GuiGraphicsExtractor graphics, List<String> lines, boolean alignLeft, CallbackInfo ci) {
        if (alignLeft) return;
        lines.add("");
        lines.add("Blueberry " + Blueberry.getVersion().getFullyQualifiedVersion());
        lines.add(Blueberry.getModLoader().getLoadedMods().size() + " mods loaded, " + Blueberry.getModLoader().getActiveMods().size() + " mods active");
        //extractLines(guiGraphics, list2, bl);
    }
}
