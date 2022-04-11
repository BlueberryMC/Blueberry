package net.blueberrymc.gradle.buildSrc.tasks

import org.gradle.api.DefaultTask

open class ApplyBlueberryPatches : DefaultTask() {
    init {
        description = "Applies the patches to the project"
    }
}
