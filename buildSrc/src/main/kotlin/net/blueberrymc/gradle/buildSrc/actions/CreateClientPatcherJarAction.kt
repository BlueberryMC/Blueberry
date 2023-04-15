package net.blueberrymc.gradle.buildSrc.actions

import io.sigpipe.jbsdiff.Diff
import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.constants.CLIENT_JAR_URL
import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import net.blueberrymc.gradle.buildSrc.util.ClasspathUtil
import net.blueberrymc.gradle.buildSrc.util.FileUtil
import net.blueberrymc.gradle.buildSrc.util.ProjectUtil.getTaskByName
import net.blueberrymc.gradle.buildSrc.util.tools.JavaCompiler
import org.apache.commons.compress.compressors.CompressorException
import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

class CreateClientPatcherJarAction : Action<BaseBlueberryTask> {
    override fun execute(task: BaseBlueberryTask) {
        val baseDir = task.project.projectDir
        val patcherJar = File(baseDir, "work/temp/jbsdiffPatcher-${System.nanoTime()}.jar")
        task.dependsOn("createClientPatch")
        task.outputs.file(patcherJar)
        task.doLast {
            val clientJarBytes = File(baseDir, "MagmaCube/work/Minecraft/$MINECRAFT_VERSION/client.jar").readBytes()
            val patchedClientJarBytes =
                task.project.getTaskByName("shadowJar", "blueberry").outputs.files.singleFile.readBytes()
            val vanillaHash = Util.sha256sum(clientJarBytes)
            val patchedHash = Util.sha256sum(patchedClientJarBytes)
            File(baseDir, "work/jbsdiffPatcher/src/main/resources/patch.properties").writeText("""
                name=blueberry
                version=$MINECRAFT_VERSION
                vanillaUrl=$CLIENT_JAR_URL
                vanillaHash=$vanillaHash
                patchedHash=$patchedHash
            """.trimIndent())
            compilePatcherJar(task.project, patcherJar)
        }
    }

    companion object {
        fun compilePatcherJar(project: Project, destination: File) {
            val jbsdiffPatcherDir = File(project.projectDir, "work/jbsdiffPatcher")
            File(jbsdiffPatcherDir, "src/main/resources/META-INF").mkdir()
            File(jbsdiffPatcherDir, "src/main/resources/META-INF/MANIFEST.MF").writeText(
                """
            |Manifest-Version: 1.0
            |Main-Class: net.blueberrymc.jbsdiffPatcher.Patcher
            |
            """.trimMargin()
            )
            JavaCompiler.logger = project.logger
            project.logger.info("Classpath of JavaCompiler: ${JavaCompiler.classpath}")
            val compiled = JavaCompiler.compileAll(File(jbsdiffPatcherDir, "src/main/java"))
            val resources = File(jbsdiffPatcherDir, "src/main/resources").toPath()

            // commons-compress
            FileUtil.shade(Paths.get(ClasspathUtil.getClasspath(CompressorException::class.java)), resources)

            // jbsdiff
            FileUtil.shade(Paths.get(ClasspathUtil.getClasspath(Diff::class.java)), resources)

            // nativeutil
            // why not use `FileUtil.shade(Paths.get(ClasspathUtil.getClasspath(NativeUtil::class.java)), resources)`?
            // because gradle automatically converts the `System.getProperty` calls, ruining the application
            Util.downloadFile("https://repo.blueberrymc.net/repository/maven-public/net/blueberrymc/native-util/2.1.0/native-util-2.1.0.jar")
                .use {
                    FileUtil.shade(it.path, resources)
                }

            // copy resources
            FileUtil.copy(resources, compiled.toPath())

            // jar
            Util.jar(destination, compiled)

            // delete temp dir
            FileUtil.deleteRecursively(compiled, deleteRoot = true)
        }
    }
}
