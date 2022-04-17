package net.blueberrymc.gradle.buildSrc.tasks

import org.gradle.api.DefaultTask

open class BaseBlueberryTask : DefaultTask() {
    init {
        group = "blueberry"
    }
}
