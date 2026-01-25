import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.minecraftforge.net/"); name = "minecraftforge" }
    maven { url = uri("https://repo.blueberrymc.net/repository/maven-public/"); name = "blueberrymc" }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.1.0.202203080745-r")

    // TODO: remove
    implementation("net.minecraftforge:forgeflower:1.5.498.29")
    implementation("net.minecraftforge:accesstransformers:8.0.4") {
        exclude("org.ow2.asm", "asm")
        exclude("org.ow2.asm", "asm-tree")
        exclude("org.ow2.asm", "asm-common")
    }
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation("org.ow2.asm:asm-analysis:9.7")
    implementation("org.ow2.asm:asm-commons:9.7")
    implementation("org.ow2.asm:asm-util:9.7")

    // ingredients for baking installer
    implementation("io.sigpipe:jbsdiff:1.0")
    implementation("net.blueberrymc:native-util:2.1.2")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
