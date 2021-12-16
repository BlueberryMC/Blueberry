plugins {
    kotlin("jvm") version "1.6.10"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.minecraftforge.net/"); name = "MinecraftForge" }
}

dependencies {
    implementation(kotlin("stdlib", "1.6.10"))
}
