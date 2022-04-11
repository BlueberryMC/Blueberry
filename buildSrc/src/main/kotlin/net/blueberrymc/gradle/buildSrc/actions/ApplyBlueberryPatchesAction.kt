package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.tasks.ApplyBlueberryPatches
import org.gradle.api.Action
import java.io.File

open class ApplyBlueberryPatchesAction : Action<ApplyBlueberryPatches> {
    override fun execute(t: ApplyBlueberryPatches) {
        t.doLast {
            Util.applyPatches(
                t.project.projectDir,
                "Blueberry-Client",
                "MagmaCube-Patches",
                File(t.project.projectDir, "MagmaCube/Minecraft"),
            )
        }
    }
}