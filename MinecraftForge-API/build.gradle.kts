import net.blueberrymc.gradle.buildSrc.constants.*

version = apiVersion

dependencies {
    compileOnly(project(":blueberry-api"))
    compileOnly("net.blueberrymc.minecraft:minecraft:$minecraftVersion")
    compileOnly("org.jetbrains:annotations:22.0.0")
}
