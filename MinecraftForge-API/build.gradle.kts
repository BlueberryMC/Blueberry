import net.blueberrymc.gradle.buildSrc.constants.*

version = apiVersion

dependencies {
    compileOnly(project(":blueberry-api"))
    compileOnly("net.blueberrymc.minecraft:minecraft:$minecraftVersion")
    compileOnly("org.jetbrains:annotations:22.0.0")
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
