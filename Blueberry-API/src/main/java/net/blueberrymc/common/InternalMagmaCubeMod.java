package net.blueberrymc.common;

import net.blueberrymc.client.resources.BlueberryText;
import net.blueberrymc.common.bml.BlueberryMod;
import net.blueberrymc.common.bml.config.BooleanVisualConfig;
import net.blueberrymc.common.bml.config.CompoundVisualConfig;
import net.blueberrymc.common.util.Versioning;
import net.blueberrymc.config.ModDescriptionFile;

import java.util.Arrays;

public class InternalMagmaCubeMod extends BlueberryMod {
    @Override
    public void onLoad() {
        this.getVisualConfig().withTitle(new BlueberryText("blueberry", "gui.screens.mod_config.test.description"));
        this.getVisualConfig().add(
                new CompoundVisualConfig(new BlueberryText("blueberry", "gui.screens.mod_config.test.nest1"))
                        .add(new CompoundVisualConfig(new BlueberryText("blueberry", "gui.screens.mod_config.test.nest2")))
        ).add(
                new BooleanVisualConfig(new BlueberryText("blueberry", "gui.screens.mod_config.test.boolean"))
        );
    }

    @SuppressWarnings("deprecation")
    public static void register() {
        ModDescriptionFile description = new ModDescriptionFile(
                "magmaCube",
                Versioning.getVersion().getMagmaCubeCommit(),
                "net.blueberrymc.common.InternalMagmaCubeMod",
                "MagmaCube",
                "MagmaCube development team",
                "Obfuscation map",
                Arrays.asList("MagmaCube allows you to decompile and understand Minecraft's code easily.", "Also, this mod is a sandbox to play with VisualConfig."),
                true,
                null);
        Blueberry.getModLoader().forceRegisterMod(description, InternalMagmaCubeMod.class);
    }
}
