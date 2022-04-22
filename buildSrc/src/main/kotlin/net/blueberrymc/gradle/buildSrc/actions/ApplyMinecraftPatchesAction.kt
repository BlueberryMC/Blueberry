package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import org.gradle.api.Action
import java.io.File

class ApplyMinecraftPatchesAction : Action<BaseBlueberryTask> {
    override fun execute(t: BaseBlueberryTask) {
        t.doLast {
            val magmaCubeDir = File(t.project.projectDir, "MagmaCube")
            if (!magmaCubeDir.isDirectory) {
                throw IllegalStateException("MagmaCube directory not found")
            }
            Util.applyPatches(
                magmaCubeDir,
                "Minecraft",
                "patches",
                File(magmaCubeDir, "work/Minecraft/$MINECRAFT_VERSION/source"),
            )
        }
    }
}