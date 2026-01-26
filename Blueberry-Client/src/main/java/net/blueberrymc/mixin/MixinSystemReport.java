package net.blueberrymc.mixin;

import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(SystemReport.class)
public abstract class MixinSystemReport {
    @Shadow
    public abstract void setDetail(String string, String string2);

    @Shadow
    public abstract void setDetail(String string, Supplier<String> supplier);

    @Inject(at = @At("TAIL"), method = "<init>")
    public void addBlueberryCrashDetails(CallbackInfo ci) {
        // Blueberry start
        this.setDetail("Blueberry Version", net.blueberrymc.common.util.Versioning.getVersion().getFullyQualifiedVersion());
        this.setDetail("Blueberry commit", net.blueberrymc.common.util.Versioning.getVersion().getCommit());
        // TODO: we need better layout
        this.setDetail("Mods", () -> {
            StringBuilder sb = new StringBuilder("\n");
            sb.append("      Status:\n");
            sb.append("        L = Loaded\n");
            sb.append("        P = Pre Init\n");
            sb.append("        I = Init\n");
            sb.append("        J = Post Init\n");
            sb.append("        A = Available\n");
            sb.append("        E = Errored\n");
            sb.append("        U = Unloaded\n");
            for (net.blueberrymc.common.bml.BlueberryMod mod : net.blueberrymc.common.Blueberry.getModLoader().getLoadedMods()) {
                sb.append("      ").append(mod.name()).append(" (").append(mod.getDescription().modId()).append(") [").append(mod.getDescription().getVersion()).append("] - ").append(mod.getStateList()).append("\n");
            }
            return sb.toString();
        });
        // Blueberry end
    }
}
