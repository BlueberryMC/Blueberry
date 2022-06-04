import net.blueberrymc.gradle.buildSrc.constants.*

version = API_VERSION

dependencies {
    compileOnly("net.blueberrymc.magmacube:magmacube:$MINECRAFT_VERSION")
    api(project(":blueberry-api"))
}
