package net.blueberrymc.gradle.buildSrc.actions

import io.sigpipe.jbsdiff.Diff
import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import net.blueberrymc.gradle.buildSrc.util.ProjectUtil.getTaskByName
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.gradle.api.Action
import java.io.File

class CreateClientPatchAction : Action<BaseBlueberryTask> {
    override fun execute(task: BaseBlueberryTask) {
        val baseDir = task.project.projectDir
        initRepository(baseDir)
        val patchFilePath = File(baseDir, "work/jbsdiffPatcher/src/main/resources/patch.bz2")
        task.outputs.file(patchFilePath)
        task.doLast {
            val clientJarBytes = File(baseDir, "MagmaCube/work/Minecraft/$MINECRAFT_VERSION/client.jar").readBytes()
            val patchedClientJarBytes =
                task.project.getTaskByName("shadowJar", "blueberry").outputs.files.singleFile.readBytes()
            patchFilePath.delete()
            patchFilePath.parentFile.mkdirs()
            patchFilePath.outputStream().use { stream -> Diff.diff(clientJarBytes, patchedClientJarBytes, stream) }
        }
    }

    companion object {
        fun initRepository(baseDir: File) {
            val jbsdiffPatcherDir = File(baseDir, "work/jbsdiffPatcher")
            val git = if (jbsdiffPatcherDir.exists()) {
                Git.open(jbsdiffPatcherDir)
            } else {
                Git.cloneRepository()
                    .setDirectory(jbsdiffPatcherDir)
                    .setBranch("main")
                    .setURI("https://github.com/BlueberryMC/jbsdiffPatcher")
                    .call()
            }
            git.fetch().call()
            git.reset().setRef("origin/main").setMode(ResetCommand.ResetType.HARD).call()
        }
    }
}
