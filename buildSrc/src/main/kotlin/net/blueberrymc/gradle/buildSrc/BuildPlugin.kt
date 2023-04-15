package net.blueberrymc.gradle.buildSrc

import net.blueberrymc.gradle.buildSrc.actions.*
import net.blueberrymc.gradle.buildSrc.tasks.ApplyBlueberryPatches
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import net.blueberrymc.gradle.buildSrc.tasks.PatchMinecraft
import net.blueberrymc.gradle.buildSrc.tasks.RebuildBlueberryPatches
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class BuildPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        File(project.rootDir, "work/temp").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        project.tasks.register("createClientPatch", BaseBlueberryTask::class.java, CreateClientPatchAction())
        project.tasks.register("createClientPatcherJar", BaseBlueberryTask::class.java, CreateClientPatcherJarAction())
        project.tasks.register("createServerPatch", BaseBlueberryTask::class.java, CreateServerPatchAction())
        project.tasks.register("createServerPatcherJar", BaseBlueberryTask::class.java, CreateServerPatcherJarAction())
        project.tasks.register("bakeInstaller", BaseBlueberryTask::class.java, BakeInstallerAction())
        project.tasks.register("downloadServerJar", BaseBlueberryTask::class.java, DownloadServerJarAction())
        project.tasks.register("patchMinecraft", PatchMinecraft::class.java, PatchMinecraftAction())
        project.tasks.register("applyBlueberryPatches", ApplyBlueberryPatches::class.java, ApplyBlueberryPatchesAction())
        project.tasks.register("applyMinecraftPatches", BaseBlueberryTask::class.java, ApplyMinecraftPatchesAction())
        project.tasks.register("rebuildBlueberryPatches", RebuildBlueberryPatches::class.java, RebuildBlueberryPatchesAction())
    }
}
