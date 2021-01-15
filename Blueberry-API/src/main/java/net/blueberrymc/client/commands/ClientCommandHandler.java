package net.blueberrymc.client.commands;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import net.blueberrymc.common.bml.BlueberryMod;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public abstract class ClientCommandHandler {
    private final BlueberryMod mod;

    public ClientCommandHandler(@NotNull BlueberryMod mod) {
        Preconditions.checkNotNull(mod, "mod cannot be null");
        this.mod = mod;
    }

    @NotNull
    public BlueberryMod getMod() {
        return mod;
    }

    public abstract void register(@NotNull CommandDispatcher<CommandSourceStack> dispatcher);
}
