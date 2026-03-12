package net.blueberrymc.client;

import net.blueberrymc.client.renderer.blockentity.MinecraftBlockEntityRenderDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class BlueberryClientImpl extends BlueberryClient {
    public static final Set<Identifier> specialModels = new HashSet<>();

    public void registerSpecialBlockEntityRenderer(@NotNull BlockEntityType<?> blockEntityType, @NotNull BlockEntityRenderer<?, ?> blockEntityRenderer) {
        ((MinecraftBlockEntityRenderDispatcher) Minecraft.getInstance().getBlockEntityRenderDispatcher())
                .registerSpecialRenderer(blockEntityType, blockEntityRenderer);
    }

    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreensFactory(@NotNull MenuType<? extends M> menuType, @NotNull ScreenConstructor<M, U> screenConstructor) {
        MenuScreens.register(menuType, screenConstructor::create);
    }

    public static void addSpecialModel(@NotNull Identifier resourceLocation) {
        specialModels.add(resourceLocation);
    }
}
