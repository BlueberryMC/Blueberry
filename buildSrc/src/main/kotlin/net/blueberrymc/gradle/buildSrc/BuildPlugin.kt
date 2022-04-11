package net.blueberrymc.gradle.buildSrc

import net.blueberrymc.gradle.buildSrc.actions.ApplyBlueberryPatchesAction
import net.blueberrymc.gradle.buildSrc.tasks.ApplyBlueberryPatches
import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("applyBlueberryPatches", ApplyBlueberryPatches::class.java, ApplyBlueberryPatchesAction())
    }
}
