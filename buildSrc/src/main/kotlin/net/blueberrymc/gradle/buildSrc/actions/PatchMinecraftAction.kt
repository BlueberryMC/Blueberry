package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import org.gradle.api.Action
import net.blueberrymc.gradle.buildSrc.tasks.PatchMinecraft
import net.blueberrymc.gradle.buildSrc.util.FileUtil
import net.blueberrymc.gradle.buildSrc.util.FileUtil.forEachFile
import org.eclipse.jgit.api.Git
import java.io.File
import net.blueberrymc.gradle.buildSrc.constants.*
import net.blueberrymc.gradle.buildSrc.util.MiniUnusedCommentRemover
import net.blueberrymc.gradle.buildSrc.util.StreamUtil.setupPrinter
import net.minecraftforge.accesstransformer.TransformerProcessor
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class PatchMinecraftAction : Action<PatchMinecraft> {
    override fun execute(task: PatchMinecraft) {
        task.doLast {
            val baseDir = it.project.projectDir
            ProcessBuilder(*"git submodule update --init".split(" ").toTypedArray())
                .start()
                .setupPrinter()
                .waitFor()
                .let { result ->
                    if (result != 0) {
                        throw RuntimeException("Failed to update submodules (child process exited with $result)")
                    }
                }
            val magmaCubeDir = File(baseDir, "MagmaCube")
            if (!magmaCubeDir.isDirectory) {
                throw IllegalStateException("MagmaCube directory not found")
            }
            ProcessBuilder(*"git submodule update --init".split(" ").toTypedArray())
                .directory(magmaCubeDir)
                .start()
                .setupPrinter()
                .waitFor()
                .let { result ->
                    if (result != 0) {
                        throw RuntimeException("Failed to update submodules in MagmaCube (child process exited with $result)")
                    }
                }
            initMagmaCube(baseDir)
            Util.applyPatches(magmaCubeDir, "Minecraft", "patches", File(magmaCubeDir, "work/Minecraft/$MINECRAFT_VERSION/source"))
        }
    }

    private fun initMagmaCube(baseDir: File) {
        val magmaCubeDir = File(baseDir, "MagmaCube")
        if (!magmaCubeDir.isDirectory) {
            throw IllegalStateException("MagmaCube directory not found")
        }
        val magmaCubePath = magmaCubeDir.toPath()
        val magmaCubeGit = Git.init().setDirectory(magmaCubeDir).setGitDir(File(baseDir, ".git/modules/MagmaCube")).call()
        magmaCubeGit.submoduleUpdate().setFetch(true).call()
        File(magmaCubeDir, "work/Minecraft/$MINECRAFT_VERSION").mkdirs()
        val clientJarFile = File(magmaCubeDir, "work/Minecraft/$MINECRAFT_VERSION/client.jar")
        val clientMappingFile = File(magmaCubeDir, "work/Minecraft/$MINECRAFT_VERSION/mapping.txt")
        // download files
        println("Download jar and mapping")
        Util.downloadFile(CLIENT_JAR_URL, clientJarFile)
        Util.downloadFile(CLIENT_MAPPING_URL, clientMappingFile)
        // run remap
        println("Remap jar")
        Util.runMain(
            File(magmaCubeDir, "work/MC-Remapper/lib").listFiles()!!.toList(),
            "io.heartpattern.mcremapper.commandline.MCRemapperAppKt",
            arrayOf(
                "--fixlocalvar=rename",
                "--output-name=${File(magmaCubeDir, "work/Minecraft/$MINECRAFT_VERSION/client-remapped.jar").absolutePath}",
                clientJarFile.absolutePath,
                clientMappingFile.absolutePath,
            )
        )
        // run PR
        Util.runMain(
            listOf(File(magmaCubeDir, "work/ParameterRemapper/ParameterRemapper-1.0.4.jar")),
            "xyz.acrylicstyle.parameterRemapper.ParameterRemapperApp",
            arrayOf(
                "--input-file=$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/client-remapped.jar",
                "--output-file=$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/client-remapped-2.jar",
                "--mapping-file=$magmaCubeDir/work/mappings/mappings/$MAPPING_VERSION.pr"
            )
        )
        // run AT
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/client-remapped.jar").delete()
        TransformerProcessor.main(
            "--inJar",
            "$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/client-remapped-2.jar",
            "--outJar",
            "$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/client-remapped-at.jar",
            "--atFile",
            "$magmaCubeDir/work/mappings/mappings/$MAPPING_VERSION.at",
        )
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/client-remapped-2.jar").delete()
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/client-remapped-at.jar"), magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/client-remapped.jar"), StandardCopyOption.REPLACE_EXISTING)
        // unzip remapped jar
        println("Unzip client-remapped.jar")
        FileUtil.unzip(
            "$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/client-remapped.jar",
            "$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/unpacked",
        )
        // write MANIFEST.MF
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/unpacked/META-INF/MANIFEST.MF").writeText(
            """
            |Manifest-Version: 1.0
            |Main-Class: net.minecraft.client.Main
            |
            |
            """.trimMargin()
        )
        // remove signature file
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/unpacked/META-INF/MOJANGCS.RSA").delete()
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/unpacked/META-INF/MOJANGCS.SF").delete()
        // decompile unzipped remapped jar
        println("Decompile client-remapped.jar")
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/source").deleteRecursively()
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/source").mkdirs()
        ConsoleDecompiler.main(
            arrayOf(
                "-dgs=1",
                "-rsy=1",
                "-ind=    ",
                "-log=WARN",
                "-mpm=30",
                "$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/unpacked",
                "$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/source"
            )
        )
        // post process decompiled jar (postDownload.sh)
        MiniUnusedCommentRemover.walk(File(magmaCubeDir, "work/Minecraft/$MINECRAFT_VERSION/source"))
        // copy files (copyFiles.sh)
        println("Copy files")
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/source/src/main/java").mkdirs()
        File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/source/src/main/resources").mkdirs()
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/source/net"), magmaCubePath.resolve("work/Minecraft/${MINECRAFT_VERSION}/source/src/main/java/net"), StandardCopyOption.ATOMIC_MOVE)
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/source/com"), magmaCubePath.resolve("work/Minecraft/${MINECRAFT_VERSION}/source/src/main/java/com"), StandardCopyOption.ATOMIC_MOVE)
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/unpacked/data"), magmaCubePath.resolve("work/Minecraft/${MINECRAFT_VERSION}/source/src/main/resources/data"), StandardCopyOption.ATOMIC_MOVE)
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/unpacked/META-INF"), magmaCubePath.resolve("work/Minecraft/${MINECRAFT_VERSION}/source/src/main/resources/META-INF"), StandardCopyOption.ATOMIC_MOVE)
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/unpacked/assets"), magmaCubePath.resolve("work/Minecraft/${MINECRAFT_VERSION}/source/src/main/resources/assets"), StandardCopyOption.ATOMIC_MOVE)
        //Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/unpacked/log4j2.xml"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/resources/log4j2.xml"), StandardCopyOption.ATOMIC_MOVE)
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/unpacked/pack.png"), magmaCubePath.resolve("work/Minecraft/${MINECRAFT_VERSION}/source/src/main/resources/pack.png"), StandardCopyOption.ATOMIC_MOVE)
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/unpacked/version.json"), magmaCubePath.resolve("work/Minecraft/${MINECRAFT_VERSION}/source/src/main/resources/version.json"), StandardCopyOption.ATOMIC_MOVE)
        Files.move(magmaCubePath.resolve("work/Minecraft/$MINECRAFT_VERSION/unpacked/flightrecorder-config.jfc"), magmaCubePath.resolve("work/Minecraft/${MINECRAFT_VERSION}/source/src/main/resources/flightrecorder-config.jfc"), StandardCopyOption.ATOMIC_MOVE)
        println("Delete unused files")
        FileUtil.deleteRecursively(File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/unpacked"), deleteRoot = true)
        println("Remove trailing newlines")
        val cs = if (System.getProperty("os.name").lowercase().contains("win")) charset("SJIS") else charset("UTF-8")
        File(magmaCubeDir, "work/Minecraft/$MINECRAFT_VERSION/source").forEachFile { file ->
            if (!file.isFile) return@forEachFile
            if (!file.name.endsWith(".java")) return@forEachFile
            val text = file.readText(cs)
            file.writeText(text.trimEnd())
        }
        // TODO: slow
        println("Setup up git")
        // setup git
        val upstreamGit = Git.init().setDirectory(File("$magmaCubeDir/work/Minecraft/$MINECRAFT_VERSION/source")).call()
        upstreamGit.add().addFilepattern("src").call()
        upstreamGit.commit()
            .setSign(false)
            .setMessage("Vanilla $ ${System.currentTimeMillis()}")
            .setAuthor("Vanilla", "auto@mated.null")
            .call()
        try {
            upstreamGit.checkout().setCreateBranch(true).setName("master").call()
        } catch (_: Exception) {
            // we can just ignore
        }
    }
}
