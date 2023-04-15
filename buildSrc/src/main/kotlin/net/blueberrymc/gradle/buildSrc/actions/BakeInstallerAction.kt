package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.constants.*
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import net.blueberrymc.gradle.buildSrc.util.FileUtil
import net.blueberrymc.gradle.buildSrc.util.ProjectUtil.getTaskByName
import net.blueberrymc.gradle.buildSrc.util.tools.JavaCompiler
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File

class BakeInstallerAction : Action<BaseBlueberryTask> {
    override fun execute(task: BaseBlueberryTask) {
        task.dependsOn("createClientPatcherJar")
        task.dependsOn("createServerPatcherJar")
        task.doLast {
            val clientPatcherJar = task.project.getTaskByName("createClientPatcherJar").outputs.files.singleFile
            val serverPatcherJar = task.project.getTaskByName("createServerPatcherJar").outputs.files.singleFile
            val patcherJar = PatcherJar(clientPatcherJar, serverPatcherJar)
            // prepare files and compile installer
            val installerJar = bakeInstaller(task.project, patcherJar)
            println()
            println("Done! Installer is now located at: ${installerJar.absolutePath}")
            println()
        }
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
