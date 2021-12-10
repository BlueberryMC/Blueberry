rootProject.name = "Blueberry-Parent"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("minecraftforge-api")
project(":minecraftforge-api").projectDir = File("MinecraftForge-API")

include("blueberry-api")
project(":blueberry-api").projectDir = File("Blueberry-API")

include("blueberry")
project(":blueberry").projectDir = File("Blueberry-Client")
