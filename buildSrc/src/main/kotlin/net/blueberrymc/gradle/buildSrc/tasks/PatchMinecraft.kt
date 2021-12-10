package net.blueberrymc.gradle.buildSrc.tasks

import org.gradle.api.DefaultTask

class PatchMinecraft : DefaultTask() {
    var clientJar = ""
    var clientObfuscationMap = ""
    var patchedSHA256 = ""

    init {
        outputs.doNotCacheIf("patchedSHA256 is empty or blank") { patchedSHA256.isBlank() }
    }
}
