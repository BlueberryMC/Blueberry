plugins {
    kotlin("jvm") version "1.6.20"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.minecraftforge.net/"); name = "minecraftforge" }
}

dependencies {
    implementation(kotlin("stdlib", "1.6.20"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.1.0.202203080745-r")
    // TODO: update forgeflower
    implementation("net.minecraftforge:forgeflower:1.5.498.14")
    implementation("net.minecraftforge:accesstransformers:8.0.4")
}
