package net.blueberrymc.mixin.client.gui.screens;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = Screen.class)
public interface MixinScreenAccessor {
    @Accessor
    List<Renderable> getRenderables();
}
