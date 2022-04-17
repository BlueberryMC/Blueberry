package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import org.gradle.api.Action
import net.blueberrymc.gradle.buildSrc.tasks.PatchMinecraft
import net.blueberrymc.gradle.buildSrc.util.FileUtil
import org.eclipse.jgit.api.Git
import java.io.File
import net.blueberrymc.gradle.buildSrc.constants.*
import net.minecraftforge.accesstransformer.TransformerProcessor
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import java.nio.file.Files

class PatchMinecraftAction : Action<PatchMinecraft> {
    override fun execute(task: PatchMinecraft) {
        task.doLast {
            val baseDir = it.project.projectDir
            val git = Git.init().setDirectory(baseDir).call()
            git.submoduleUpdate().setFetch(true).call()
            val magmaCubeDir = File(baseDir, "MagmaCube")
            if (!magmaCubeDir.isDirectory) {
                throw IllegalStateException("MagmaCube directory not found")
            }
            val magmaCubeGit = Git.init().setDirectory(magmaCubeDir).call()
            magmaCubeGit.submoduleUpdate().setFetch(true).call()
            initMagmaCube(baseDir)
        }
    }

    private fun initMagmaCube(baseDir: File) {
        val magmaCubeDir = File(baseDir, "MagmaCube")
        if (!magmaCubeDir.isDirectory) {
            throw IllegalStateException("MagmaCube directory not found")
        }
        val magmaCubePath = magmaCubeDir.toPath()
        val magmaCubeGit = Git.init().setDirectory(magmaCubeDir).call()
        magmaCubeGit.submoduleUpdate().setFetch(true).call()
        val clientJarFile = File(magmaCubeDir, "work/Minecraft/$minecraftVersion/client.jar")
        val clientMappingFile = File(magmaCubeDir, "work/Minecraft/$minecraftVersion/mapping.txt")
        // download files
        println("Download jar and mapping")
        Util.downloadFile(clientJar, clientJarFile)
        Util.downloadFile(clientMapping, clientMappingFile)
        // run remap
        println("Remap jar")
        Util.runMain(
            File(magmaCubeDir, "work/MC-Remapper").listFiles()!!.toList(),
            "io.heartpattern.mcremapper.commandline.MCRemapperAppKt",
            arrayOf(
                "--fixlocalvar=rename",
                "--output-name=${File(magmaCubeDir, "work/Minecraft/$minecraftVersion/client-remapped.jar").absolutePath}",
                clientJarFile.absolutePath,
                clientMappingFile.absolutePath,
            )
        )
        // run PR
        Util.runMain(
            File(magmaCubeDir, "work/ParameterRemapper/ParameterRemapper-1.0.3.jar").listFiles()!!.toList(),
            "xyz.acrylicstyle.parameterRemapper.ParameterRemapperApp",
            arrayOf(
                "--input-file=$magmaCubeDir/work/Minecraft/$minecraftVersion/client-remapped.jar",
                "--output-file=$magmaCubeDir/work/Minecraft/$minecraftVersion/client-remapped-2.jar",
                "--mapping-file=$magmaCubeDir/work/mappings/mappings/$mappingVersion.pr\""
            )
        )
        // run AT
        TransformerProcessor.main(
            "--inJar",
            "$magmaCubeDir/work/Minecraft/$minecraftVersion/client-remapped-2.jar",
            "--outJar",
            "$magmaCubeDir/work/Minecraft/$minecraftVersion/client-remapped.jar",
            "--atFile",
            "$magmaCubeDir/work/mappings/mappings/$mappingVersion.at",
        )
        File("$magmaCubeDir/work/Minecraft/$minecraftVersion/client-remapped-2.jar").delete()
        // unzip remapped jar
        println("Unzip client-remapped.jar")
        FileUtil.unzip(
            "$magmaCubeDir/work/Minecraft/$minecraftVersion/client-remapped.jar",
            "$magmaCubeDir/work/Minecraft/$minecraftVersion/unpacked",
        )
        // write MANIFEST.MF
        File("$magmaCubeDir/work/Minecraft/$minecraftVersion/unpacked/META-INF/MANIFEST.MF").writeText(
            """
            |Manifest-Version: 1.0
            |Main-Class: net.minecraft.client.main.Main
            |
            """.trimMargin()
        )
        // remove signature file
        File("$magmaCubeDir/work/Minecraft/$minecraftVersion/unpacked/META-INF/MOJANGCS.RSA").delete()
        File("$magmaCubeDir/work/Minecraft/$minecraftVersion/unpacked/META-INF/MOJANGCS.SF").delete()
        // decompile unzipped remapped jar
        println("Decompile client-remapped.jar")
        File("$magmaCubeDir/work/Minecraft/$minecraftVersion/source").deleteRecursively()
        ConsoleDecompiler.main(
            arrayOf(
                "-dgs=1",
                "-rsy=1",
                "-ind=\"    \"",
                "-log=\"WARN\"",
                "-mpm=30",
                "$magmaCubeDir/work/Minecraft/$minecraftVersion/unpacked",
                "$magmaCubeDir/work/Minecraft/$minecraftVersion/source"
            )
        )
        // post process decompiled jar (postDownload.sh)
        Util.runMain(
            listOf(File("$magmaCubeDir/work/UnusedCommentRemover/UnusedCommentRemover-1.0.10.jar")),
            "xyz.acrylicstyle.unusedCommentRemover.Main",
            arrayOf("--output-dir=$magmaCubeDir/work/Minecraft/$minecraftVersion/source"),
        )
        // copy files (copyFiles.sh)
        println("Copy files")
        File("$magmaCubeDir/Minecraft/src/main/java").deleteRecursively()
        File("$magmaCubeDir/Minecraft/src/main/resources").deleteRecursively()
        File("$magmaCubeDir/work/Minecraft/$minecraftVersion/source/src/main/java").mkdirs()
        File("$magmaCubeDir/work/Minecraft/$minecraftVersion/source/src/main/resources").mkdirs()
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/source/net"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/java/net"))
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/source/com"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/java/com"))
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/unpacked/data"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/resources/data"))
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/unpacked/META-INF"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/resources/META-INF"))
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/unpacked/assets"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/resources/assets"))
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/unpacked/log4j2.xml"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/resources/log4j2.xml"))
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/unpacked/pack.png"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/resources/pack.png"))
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/unpacked/version.json"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/resources/version.json"))
        Files.move(magmaCubePath.resolve("work/Minecraft/$minecraftVersion/unpacked/flightrecorder-config.jfc"), magmaCubePath.resolve("work/Minecraft/${minecraftVersion}/source/src/main/resources/flightrecorder-config.jfc"))
        File("$magmaCubeDir/work/Minecraft/$minecraftVersion/unpacked").deleteRecursively()
        File(magmaCubeDir, "work/Minecraft/$minecraftVersion/source/src/main/java").copyRecursively(File(magmaCubeDir, "Minecraft/src/main/java"))
        File(magmaCubeDir, "work/Minecraft/$minecraftVersion/source/src/main/resources").copyRecursively(File(magmaCubeDir, "Minecraft/src/main/resources"))
        // setup git
        val upstreamGit = Git.init().setDirectory(File("$magmaCubeDir/work/Minecraft/$minecraftVersion/source")).call()
        upstreamGit.add().addFilepattern("src").call()
        upstreamGit.commit().setMessage("Vanilla $ ${System.currentTimeMillis()}").setAuthor("Vanilla", "auto@mated.null").call()
        upstreamGit.checkout().setCreateBranch(true).setName("master").call()
    }
}
