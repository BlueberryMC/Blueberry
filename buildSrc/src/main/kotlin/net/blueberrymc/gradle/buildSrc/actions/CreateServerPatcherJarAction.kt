package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.constants.CLIENT_JAR_URL
import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import net.blueberrymc.gradle.buildSrc.util.ProjectUtil.getTaskByName
import org.gradle.api.Action
import java.io.File

class CreateServerPatcherJarAction : Action<BaseBlueberryTask> {
    override fun execute(task: BaseBlueberryTask) {
        val baseDir = task.project.projectDir
        val patcherJar = File(baseDir, "work/temp/jbsdiffPatcher-${System.nanoTime()}.jar")
        task.dependsOn("createServerPatch")
        task.outputs.file(patcherJar)
        task.doLast {
            val serverJarBytes = task.project.getTaskByName("downloadServerJar").outputs.files.singleFile.readBytes()
            val patchedServerJarBytes =
                task.project.getTaskByName("shadowServerJar", "blueberry").outputs.files.singleFile.readBytes()
            val vanillaHash = Util.sha256sum(serverJarBytes)
            val patchedHash = Util.sha256sum(patchedServerJarBytes)
            File(baseDir, "work/jbsdiffPatcher/src/main/resources/patch.properties").writeText("""
                name=blueberry
                version=$MINECRAFT_VERSION
                vanillaUrl=$CLIENT_JAR_URL
                vanillaHash=$vanillaHash
                patchedHash=$patchedHash
            """.trimIndent())
            CreateClientPatcherJarAction.compilePatcherJar(task.project, patcherJar)
        }
    }
}
