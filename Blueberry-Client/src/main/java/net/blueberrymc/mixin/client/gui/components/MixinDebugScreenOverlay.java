package net.blueberrymc.mixin.client.gui.components;

import net.blueberrymc.common.Blueberry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = DebugScreenOverlay.class, remap = false)
public abstract class MixinDebugScreenOverlay {
    @Shadow
    protected abstract void renderLines(GuiGraphics guiGraphics, List<String> list, boolean bl);

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;renderLines(Lnet/minecraft/client/gui/GuiGraphics;Ljava/util/List;Z)V"), method = "drawSystemInformation")
    public void renderBlueberryLines(DebugScreenOverlay instance, GuiGraphics guiGraphics, List<String> list, boolean bl) {
        List<String> list2 = new ArrayList<>(list);
        list2.add("");
        list2.add("Blueberry " + Blueberry.getVersion().getFullyQualifiedVersion());
        list2.add(Blueberry.getModLoader().getLoadedMods().size() + " mods loaded, " + Blueberry.getModLoader().getActiveMods().size() + " mods active");
        renderLines(guiGraphics, list2, bl);
    }
}
