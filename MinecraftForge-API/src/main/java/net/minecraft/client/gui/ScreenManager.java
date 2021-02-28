package net.minecraft.client.gui;

import net.blueberrymc.client.BlueberryClient;
import net.blueberrymc.common.Blueberry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ScreenManager {
    public static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerFactory(MenuType<? extends M> menuType, BlueberryClient.ScreenConstructor<M, U> screenConstructor) {
        Blueberry.getUtil().asClient().registerMenuScreensFactory(menuType, screenConstructor);
    }
}
