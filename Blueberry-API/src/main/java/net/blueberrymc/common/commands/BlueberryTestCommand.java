package net.blueberrymc.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;

public class BlueberryTestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("blueberrytest")
                        .then(Commands.literal("hasModel")
                                .then(Commands.argument("item", ItemArgument.item())
                                        .executes(context -> executeHasModel(context.getSource(), ItemArgument.getItem(context, "item")))
                                )
                        )
        );
    }

    public static int executeHasModel(CommandSourceStack source, ItemInput itemInput) {
        Item item = itemInput.getItem();
        ItemModelShaper itemModelShaper = new ItemModelShaper(Minecraft.getInstance().getModelManager());

        for(Item i : Registry.ITEM) {
            itemModelShaper.register(i, new ModelResourceLocation(Registry.ITEM.getKey(i), "inventory"));
        }
        BakedModel model = itemModelShaper.getItemModel(item);
        Component name;
        try {
            name = item.getName(null);
        } catch (NullPointerException ex) {
            name = new TextComponent(item.getDescriptionId());
        }
        source.sendSuccess(new TextComponent("").append(name).append(" has model: " ).append(Boolean.toString(model != null)), true);
        return 1;
    }
}
