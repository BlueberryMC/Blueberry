plugins {
    kotlin("jvm") version "1.9.10"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.minecraftforge.net/"); name = "minecraftforge" }
    maven { url = uri("https://repo.blueberrymc.net/repository/maven-public/"); name = "blueberrymc" }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.1.0.202203080745-r")
    implementation("net.minecraftforge:forgeflower:1.5.498.29")
    implementation("net.minecraftforge:accesstransformers:8.0.4")

    // ingredients for baking installer
    implementation("io.sigpipe:jbsdiff:1.0")
    implementation("net.blueberrymc:native-util:2.1.0")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = compileJava.get().targetCompatibility
        }
    }
}
