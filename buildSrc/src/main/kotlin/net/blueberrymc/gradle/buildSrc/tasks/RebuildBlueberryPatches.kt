package net.blueberrymc.gradle.buildSrc.tasks

import org.gradle.api.DefaultTask

open class RebuildBlueberryPatches : DefaultTask() {
    init {
        group = "blueberry"
        description = "Applies the patches to the project"
    }
}
