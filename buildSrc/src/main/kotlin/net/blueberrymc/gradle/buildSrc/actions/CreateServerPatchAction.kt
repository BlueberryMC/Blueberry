package net.blueberrymc.gradle.buildSrc.actions

import io.sigpipe.jbsdiff.Diff
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import net.blueberrymc.gradle.buildSrc.util.ProjectUtil.getTaskByName
import org.gradle.api.Action
import java.io.File

class CreateServerPatchAction : Action<BaseBlueberryTask> {
    override fun execute(task: BaseBlueberryTask) {
        val baseDir = task.project.projectDir
        CreateClientPatchAction.initRepository(baseDir)
        val patchFilePath = File(baseDir, "work/jbsdiffPatcher/src/main/resources/patch.bz2")
        task.dependsOn("downloadServerJar")
        task.dependsOn("shadowServerJar")
        task.outputs.file(patchFilePath)
        task.doLast {
            val serverJarBytes = task.project.getTaskByName("downloadServerJar").outputs.files.singleFile.readBytes()
            val patchedServerJarBytes =
                task.project.getTaskByName("shadowServerJar", "blueberry").outputs.files.singleFile.readBytes()
            patchFilePath.delete()
            patchFilePath.parentFile.mkdirs()
            patchFilePath.outputStream().use { stream -> Diff.diff(serverJarBytes, patchedServerJarBytes, stream) }
        }
    }
}
