plugins {
    id("fabric-loom") version "1.15-SNAPSHOT"
    `maven-publish`
}

version = project.properties["minecraft_version"] as String
group = "net.blueberrymc"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
}

dependencies {
    api(project(":blueberry-api"))
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings(loom.officialMojangMappings())
//    annotationProcessor("com.google.code.gson:gson:2.10")
//    annotationProcessor("com.google.guava:guava:31.1-jre")
//    annotationProcessor("org.ow2.asm:asm:9.7")
//    annotationProcessor("org.ow2.asm:asm-tree:9.7")
//    annotationProcessor("org.ow2.asm:asm-analysis:9.7")
//    annotationProcessor("org.ow2.asm:asm-commons:9.7")
//    annotationProcessor("org.ow2.asm:asm-util:9.7")
//    annotationProcessor("org.spongepowered:mixin:0.8.7")
    compileOnlyApi("net.fabricmc:fabric-loader:0.18.4")
    runtimeOnly(loom.namedMinecraftJars)
    //mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"]}:v2")
}

java {
    withSourcesJar()
}

loom {
    accessWidenerPath = file("../Blueberry-API/src/main/resources/blueberry.accesswidener")
}

tasks {
    runClient {
        mainClass.set("net.blueberrymc.client.main.ClientMain")
        jvmArgs("-Dmixin.debug=true")
        args("--accessToken=foobar", "--version=${project.properties["minecraft_version"]}")
    }
}
