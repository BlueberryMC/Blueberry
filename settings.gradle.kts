rootProject.name = "Blueberry-Parent"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

include("blueberry-api")
project(":blueberry-api").projectDir = File("Blueberry-API")

include("blueberry")
project(":blueberry").projectDir = File("Blueberry-Client")
