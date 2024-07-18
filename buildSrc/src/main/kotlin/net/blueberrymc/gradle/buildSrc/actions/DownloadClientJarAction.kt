package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.constants.CLIENT_JAR_URL
import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import org.gradle.api.Action
import java.io.File

class DownloadClientJarAction : Action<BaseBlueberryTask> {
    override fun execute(task: BaseBlueberryTask) {
        val destination = File(task.project.projectDir, "work/Minecraft/$MINECRAFT_VERSION/client.jar")
        task.outputs.file(destination)
        task.doLast {
            Util.downloadFile(CLIENT_JAR_URL, destination)
        }
    }
}
