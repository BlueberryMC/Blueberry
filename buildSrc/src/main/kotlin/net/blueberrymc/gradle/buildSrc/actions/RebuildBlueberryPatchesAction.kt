package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.tasks.RebuildBlueberryPatches
import org.gradle.api.Action

open class RebuildBlueberryPatchesAction : Action<RebuildBlueberryPatches> {
    override fun execute(t: RebuildBlueberryPatches) {
        t.doLast {
            Util.rebuildPatches(
                t.project.projectDir,
                "Blueberry-Client",
                "MagmaCube-Patches",
            )
        }
    }
}
