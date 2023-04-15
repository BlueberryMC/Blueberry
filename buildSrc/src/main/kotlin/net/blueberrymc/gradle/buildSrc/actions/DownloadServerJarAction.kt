package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.constants.SERVER_JAR_URL
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import org.gradle.api.Action
import java.io.File

class DownloadServerJarAction : Action<BaseBlueberryTask> {
    override fun execute(task: BaseBlueberryTask) {
        val destination = File.createTempFile("download", ".tmp")
        task.outputs.file(destination)
        task.doLast {
            Util.downloadFile(SERVER_JAR_URL, destination)
        }
    }
}
