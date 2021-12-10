package net.blueberrymc.gradle.buildSrc.actions

import org.gradle.api.Action
import net.blueberrymc.gradle.buildSrc.tasks.PatchMinecraft

class PatchMinecraftAction : Action<PatchMinecraft> {
    override fun execute(task: PatchMinecraft) {
        task.doLast {
            if (task.patchedSHA256.isBlank()) {
                task.logger.info("patchedSHA256 is empty or blank - skipping validation")
            }
        }
    }
}
