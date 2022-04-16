package net.blueberrymc.gradle.buildSrc.actions

import net.blueberrymc.gradle.buildSrc.Util
import org.gradle.api.Action
import net.blueberrymc.gradle.buildSrc.tasks.PatchMinecraft
import org.eclipse.jgit.api.Git
import java.io.File
import net.blueberrymc.gradle.buildSrc.constants.*
import net.minecraftforge.accesstransformer.TransformerProcessor

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
        }
    }

    fun initMagmaCube(baseDir: File) {
        val magmaCubeDir = File(baseDir, "MagmaCube")
        if (!magmaCubeDir.isDirectory) {
            throw IllegalStateException("MagmaCube directory not found")
        }
        val magmaCubeGit = Git.init().setDirectory(magmaCubeDir).call()
        magmaCubeGit.submoduleUpdate().setFetch(true).call()
        val clientJarFile = File(magmaCubeDir, "work/Minecraft/$minecraftVersion/client.jar")
        val clientMappingFile = File(magmaCubeDir, "work/Minecraft/$minecraftVersion/mapping.txt")
        Util.downloadFile(clientJar, clientJarFile)
        Util.downloadFile(clientMapping, clientMappingFile)
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
        Util.runMain(
            File(magmaCubeDir, "work/ParameterRemapper/ParameterRemapper-1.0.3.jar").listFiles()!!.toList(),
            "xyz.acrylicstyle.parameterRemapper.ParameterRemapperApp",
            arrayOf(
                "--input-file=$magmaCubeDir/work/Minecraft/$minecraftVersion/client-remapped.jar",
                "--output-file=$magmaCubeDir/work/Minecraft/$minecraftVersion/client-remapped-2.jar",
                "--mapping-file=$magmaCubeDir/work/mappings/mappings/$mappingVersion.pr\""
            )
        )
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
        // write MANIFEST.MF
        // remove signature file
        // decompile unzipped remapped jar
        // post process decompiled jar (postDownload.sh)
        // copy files (copyFiles.sh)
        // setup git
    }
}
