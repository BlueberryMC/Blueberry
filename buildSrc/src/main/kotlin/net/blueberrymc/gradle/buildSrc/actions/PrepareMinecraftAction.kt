package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import net.blueberrymc.gradle.buildSrc.tasks.ApplyBlueberryPatches
import net.blueberrymc.gradle.buildSrc.tasks.BaseBlueberryTask
import org.eclipse.jgit.api.Git
import org.gradle.api.Action
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.text.SimpleDateFormat

open class PrepareMinecraftAction : Action<BaseBlueberryTask> {
    private val files = listOf(
        "work/Minecraft-files/build.gradle.kts",
        "work/Minecraft-files/gradle.properties",
        "work/Minecraft-files/settings.gradle.kts",
        "gradle",
        "gradlew",
        "gradlew.bat",
    )

    override fun execute(t: BaseBlueberryTask) {
        t.doLast {
            val minecraftRoot = File(t.project.rootDir, "work/Minecraft")
            // setup files
            minecraftRoot.mkdirs()
            minecraftRoot.resolve("src").let {
                if (it.exists()) it.deleteRecursively()
            }
            minecraftRoot.resolve(".git").let {
                if (it.exists()) it.deleteRecursively()
            }
            files.forEach { file ->
                val fileName = file.split("/").last()
                val from = File(t.project.rootDir, file)
                if (from.isDirectory) {
                    from.copyRecursively(File(minecraftRoot, fileName), true)
                } else {
                    from.copyTo(File(minecraftRoot, fileName), true)
                    if (fileName == "gradlew" && !System.getProperty("os.name").lowercase().contains("win")) {
                        Files.setPosixFilePermissions(
                            File(minecraftRoot, fileName).toPath(),
                            PosixFilePermissions.fromString("rwxrwxrwx")
                        )
                    }
                }
            }
            val isWindows = System.getProperty("os.name").lowercase().contains("win")
            val exitCode = ProcessBuilder()
                .directory(minecraftRoot)
                .command(*if (isWindows) arrayOf("gradlew.bat", "genSources", "unzipSourcesJar") else arrayOf("bash", "-c", "./gradlew genSources unzipSourcesJar"))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()
                .apply {
                    Thread {
                        var read: String?
                        do {
                            read = inputReader().readLine()
                            if (read != null) {
                                println(read)
                            }
                        } while (read != null)
                    }.start()
                    Thread {
                        var read: String?
                        do {
                            read = errorReader().readLine()
                            if (read != null) {
                                println(read)
                            }
                        } while (read != null)
                    }.start()
                }
                .waitFor()
            if (exitCode != 0) {
                error("Gradle process failed with code $exitCode")
            }
            // setup git
            val git = Git.init().setInitialBranch("master").setDirectory(minecraftRoot).call()
            git.add().addFilepattern(".").call()
            git.commit().setMessage("Vanilla $ ${Util.getMojangDateTime()}").setAuthor("Gradle", "auto@mated.null").setSign(false).call()
        }
    }
}
