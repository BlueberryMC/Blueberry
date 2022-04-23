package net.blueberrymc.gradle.buildSrc.actions

import io.sigpipe.jbsdiff.DefaultDiffSettings
import io.sigpipe.jbsdiff.Diff
import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.constants.API_VERSION
import net.blueberrymc.gradle.buildSrc.constants.CLIENT_JAR_URL
import net.blueberrymc.gradle.buildSrc.constants.SERVER_JAR_URL
import net.blueberrymc.gradle.buildSrc.constants.MINECRAFT_VERSION
import net.blueberrymc.gradle.buildSrc.constants.SERVER_REPOSITORIES_LIST
import net.blueberrymc.gradle.buildSrc.constants.SERVER_LIBRARIES_LIST
import net.blueberrymc.gradle.buildSrc.constants.SERVER_LIBRARIES_EXCLUDES_LIST
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import net.blueberrymc.gradle.buildSrc.util.ClasspathUtil
import net.blueberrymc.gradle.buildSrc.util.FileUtil
import net.blueberrymc.gradle.buildSrc.util.tools.JavaCompiler
import org.apache.commons.compress.compressors.CompressorException
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.readBytes

class BakeInstallerAction : Action<BaseBlueberryTask> {
    override fun execute(task: BaseBlueberryTask) {
        task.project.subprojects.find { it.name == "blueberry" }?.also {
            task.dependsOn(it.tasks.getByName("shadowJar"))
            task.dependsOn(it.tasks.getByName("shadowServerJar"))
        }
        task.doLast {
            // create patch + compile jbsdiffPatcher
            val patcherJar = createPatchFile(task.project)
            // prepare files and compile installer
            val installerJar = bakeInstaller(task.project, patcherJar)
            println()
            println("Done! Installer is now located at: ${installerJar.absolutePath}")
            println()
        }
    }

    // https://github.com/BlueberryMC/Blueberry/blob/2f498220ce1f6df16cef959f6420fe0e9fe8ef99/scripts/createPatchFile.sh
    // in: project
    // out: patcher jar
    private fun createPatchFile(project: Project): PatcherJar {
        val baseDir = project.projectDir
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
        return PatcherJar(createClientPatcherJar(project), createServerPatcherJar(project))
    }

    private fun createClientPatcherJar(project: Project): File {
        val baseDir = project.projectDir
        val clientJarBytes = File(baseDir, "MagmaCube/work/Minecraft/$MINECRAFT_VERSION/client.jar").readBytes()
        val patchedClientJarBytes =
            project.subprojects
                .find { it.name == "blueberry" }!!
                .tasks
                .getByName("shadowJar")
                .outputs
                .files
                .singleFile
                .readBytes()
        val patchFilePath = File(baseDir, "work/jbsdiffPatcher/src/main/resources/patch.bz2")
        patchFilePath.delete()
        println("Creating patch (client)")
        patchFilePath.parentFile.mkdirs()
        patchFilePath.outputStream().use { stream ->
            Diff.diff(
                clientJarBytes,
                patchedClientJarBytes,
                stream,
                DefaultDiffSettings(CompressorStreamFactory.XZ),
            )
        }
        println("Creating patcher jar")
        val vanillaHash = Util.sha256sum(clientJarBytes)
        val patchedHash = Util.sha256sum(patchedClientJarBytes)
        File(baseDir, "work/jbsdiffPatcher/src/main/resources/patch.properties").writeText("""
            name=blueberry
            version=$MINECRAFT_VERSION
            vanillaUrl=$CLIENT_JAR_URL
            vanillaHash=$vanillaHash
            patchedHash=$patchedHash
        """.trimIndent())
        return compilePatcherJar(project)
    }

    private fun createServerPatcherJar(project: Project): File {
        val baseDir = project.projectDir
        println("Downloading server jar")
        val serverJarBytes = Util.downloadFile(SERVER_JAR_URL).use { it.path.readBytes() }
        val patchedServerJarBytes =
            project.subprojects
                .find { it.name == "blueberry" }!!
                .tasks
                .getByName("shadowServerJar")
                .outputs
                .files
                .singleFile
                .readBytes()
        val patchFilePath = File(baseDir, "work/jbsdiffPatcher/src/main/resources/patch.bz2")
        patchFilePath.delete()
        println("Creating patch (server)")
        patchFilePath.outputStream().use { stream ->
            Diff.diff(
                serverJarBytes,
                patchedServerJarBytes,
                stream,
            )
        }
        println("Creating patcher jar")
        val vanillaHash = Util.sha256sum(serverJarBytes)
        val patchedHash = Util.sha256sum(patchedServerJarBytes)
        File(baseDir, "work/jbsdiffPatcher/src/main/resources/patch.properties").writeText("""
            name=blueberry
            version=$MINECRAFT_VERSION
            vanillaUrl=$SERVER_JAR_URL
            vanillaHash=$vanillaHash
            patchedHash=$patchedHash
        """.trimIndent())
        return compilePatcherJar(project)
    }

    private fun compilePatcherJar(project: Project): File {
        val jbsdiffPatcherDir = File(project.projectDir, "work/jbsdiffPatcher")
        File(jbsdiffPatcherDir, "src/main/resources/META-INF").mkdir()
        File(jbsdiffPatcherDir, "src/main/resources/META-INF/MANIFEST.MF").writeText("""
            |Manifest-Version: 1.0
            |Main-Class: net.blueberrymc.jbsdiffPatcher.Patcher
            |
            """.trimMargin())
        JavaCompiler.logger = project.logger
        project.logger.info("Classpath of JavaCompiler: ${JavaCompiler.classpath}")
        val compiled = JavaCompiler.compileAll(File(jbsdiffPatcherDir, "src/main/java"))
        val resources = File(jbsdiffPatcherDir, "src/main/resources").toPath()
        val patcherJar = File(project.projectDir, "work/temp/jbsdiffPatcher.jar")
        println("Shading commons-compress")
        FileUtil.shade(Paths.get(ClasspathUtil.getClasspath(CompressorException::class.java)), resources)
        println("Shading jbsdiff")
        FileUtil.shade(Paths.get(ClasspathUtil.getClasspath(Diff::class.java)), resources)
        // why not use `FileUtil.shade(Paths.get(ClasspathUtil.getClasspath(NativeUtil::class.java)), resources)`?
        // because gradle automatically converts the `System.getProperty` calls, ruining the application
        Util.downloadFile("https://repo.blueberrymc.net/repository/maven-public/net/blueberrymc/native-util/2.1.0/native-util-2.1.0.jar").use {
            println("Shading ${it.path}")
            FileUtil.shade(it.path, resources)
        }
        FileUtil.copy(resources, compiled.toPath())
        Util.jar(patcherJar, compiled)
        FileUtil.deleteRecursively(compiled, deleteRoot = true)
        return patcherJar
    }

    private fun bakeInstaller(project: Project, patcherJar: PatcherJar): File {
        val baseDir = project.projectDir
        val installerDir = File(baseDir, "work/Installer")
        val git = if (installerDir.exists()) {
            Git.open(installerDir)
        } else {
            Git.cloneRepository()
                .setDirectory(installerDir)
                .setBranch("main")
                .setURI("https://github.com/BlueberryMC/Installer")
                .call()
        }
        git.fetch().call()
        git.reset().setRef("origin/main").setMode(ResetCommand.ResetType.HARD).call()
        val apiVersionWithoutSnapshot = API_VERSION.replace("-SNAPSHOT", "")
        val name = "blueberry-$MINECRAFT_VERSION-$apiVersionWithoutSnapshot.${Util.getBuildNumber(project)}"
        val datetime = Util.getMojangDateTime()
        File(installerDir, "src/main/resources/profile.properties").writeText("""
            name=$name
            serverFiles=server.jar
            serverRepositories=/server_repositories.list
            serverLibraries=/server_libraries.list
            serverLibrariesExclude=/server_libraries_excludes.list
            extractFiles=server.jar,client.jar,client.json,profile.properties
        """.trimIndent())
        patcherJar.client.copyTo(File(installerDir, "src/main/resources/client.jar"), true)
        patcherJar.server.copyTo(File(installerDir, "src/main/resources/server.jar"), true)
        File(installerDir, "src/main/resources/server_repositories.list").writeText(SERVER_REPOSITORIES_LIST.joinToString("\n"))
        File(installerDir, "src/main/resources/server_libraries.list").writeText(SERVER_LIBRARIES_LIST.joinToString("\n"))
        File(installerDir, "src/main/resources/server_libraries_excludes.list").writeText(SERVER_LIBRARIES_EXCLUDES_LIST.joinToString("\n"))
        File(baseDir, "scripts/files/version.json").copyTo(File(installerDir, "src/main/resources/client.json"), true)
        File(installerDir, "src/main/resources/client.json").appendText("""
                "releaseTime": "$datetime",
                "time": "$datetime",
                "mainClass": "net.blueberrymc.client.main.ClientMain",
                "id": "$name"
            }
        """.trimIndent())
        File(installerDir, "src/main/resources/META-INF").mkdir()
        File(installerDir, "src/main/resources/META-INF/MANIFEST.MF").writeText("""
            |Manifest-Version: 1.0
            |Main-Class: net.blueberrymc.installer.Installer
            |
        """.trimMargin())
        val installerJar = File(baseDir, "$name-installer.jar")
        val compiled = JavaCompiler.compileAll(File(installerDir, "src/main/java"))
        FileUtil.copy(File(installerDir, "src/main/resources").toPath(), compiled.toPath())
        Util.jar(installerJar, compiled)
        FileUtil.deleteRecursively(compiled, deleteRoot = true)
        return installerJar
    }

    private data class PatcherJar(val client: File, val server: File)
}
