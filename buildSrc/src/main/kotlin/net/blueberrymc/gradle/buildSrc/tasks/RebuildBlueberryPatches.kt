package net.blueberrymc.gradle.buildSrc.tasks

open class RebuildBlueberryPatches : BaseBlueberryTask() {
    init {
        description = "Applies the patches to the project"
    }
}
