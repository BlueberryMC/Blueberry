plugins {
    kotlin("jvm") version "1.6.20"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.minecraftforge.net/"); name = "MinecraftForge" }
}

dependencies {
    implementation(kotlin("stdlib", "1.6.20"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.1.0.202203080745-r")
}
