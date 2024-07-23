import net.blueberrymc.gradle.buildSrc.constants.*
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact
import org.gradle.jvm.component.internal.DefaultJvmSoftwareComponent
import org.gradle.jvm.component.internal.JvmSoftwareComponentInternal

plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
}

version = API_VERSION

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    mappings(loom.officialMojangMappings())

    implementation("org.jetbrains:annotations:24.0.1")
    compileOnlyApi("org.jetbrains:annotations:24.0.1")
    api("com.github.JnCrMx:discord-game-sdk4j:v0.5.5")
    api("com.google.code.findbugs:jsr305:3.0.2")
    api("com.google.code.gson:gson:2.10")
    api("com.google.guava:guava:31.1-jre")
    api("com.mojang:brigadier:1.1.8")
    api("com.mojang:datafixerupper:6.0.8")
    api("com.mojang:authlib:5.0.47")
    api("it.unimi.dsi:fastutil:8.5.12")
    api("io.netty:netty-all:4.1.97.Final")
    api("org.apache.commons:commons-lang3:3.13.0")
    api("org.yaml:snakeyaml:2.2")
    api("org.ow2.asm:asm:9.7")
    api("org.ow2.asm:asm-tree:9.7")
    api("org.ow2.asm:asm-analysis:9.7")
    api("org.ow2.asm:asm-commons:9.7")
    api("org.ow2.asm:asm-util:9.7")
    api("org.spongepowered:mixin:0.8.7")
    api("net.blueberrymc:native-util:2.1.0")
    api("ca.weblite:java-objc-bridge:1.1")
    api("org.joml:joml:1.10.5")
    api("net.minecraft:launchwrapper:1.12") {
        exclude("org.lwjgl.lwjgl", "lwjgl")
        exclude("org.lwjgl", "lwjgl")
        exclude("org.lwjgl", "lwjgl-openal")
        exclude("org.ow2.asm", "asm-debug-all")
    }
}

publishing {
    repositories {
        maven {
            name = "blueberryRepo"
            credentials(PasswordCredentials::class)
            url = uri(
                if (project.version.toString().endsWith("-SNAPSHOT"))
                    "https://repo.blueberrymc.net/repository/maven-snapshots/"
                else
                    "https://repo.blueberrymc.net/repository/maven-releases/"
            )
        }
    }
}

(components.getByName("java") as DefaultJvmSoftwareComponent).let {
    it.mainFeature.apiElementsConfiguration.artifacts.forEach { pa ->
        if (pa is ArchivePublishArtifact) {
            ArchivePublishArtifact::class.java.getDeclaredField("archiveTask")
                .apply { isAccessible = true }
                .set(pa, tasks.jar.get())
        }
    }
}

tasks {
    processResources {
        filesMatching("**/*.json") {
            filter(org.apache.tools.ant.filters.EscapeUnicode::class.java)
        }
    }

    remapJar {
        enabled = false
    }

    jar {
        // restore default configuration
        destinationDirectory.set(destinationDirectory.get().asFile.parentFile.resolve("libs"))
        archiveClassifier.set("")
    }
}
