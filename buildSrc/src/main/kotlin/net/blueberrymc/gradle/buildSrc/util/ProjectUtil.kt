package net.blueberrymc.gradle.buildSrc.util

import org.gradle.api.Project
import org.gradle.api.Task

object ProjectUtil {
    fun Project.getTaskByName(task: String, subproject: String? = null): Task =
        if (subproject != null) {
            subprojects.find { it.name == subproject }!!.tasks.getByName(task)
        } else {
            tasks.getByName(task)
        }
}
