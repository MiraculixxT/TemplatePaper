import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("io.papermc.paperweight.userdev") version "1.5.11"
    id("xyz.jpenilla.run-paper") version "2.2.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.modrinth.minotaur") version "2.+"

//    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = properties["group"] as String
version = properties["version"] as String
description = properties["description"] as String

val gameVersion by properties
val foliaSupport = properties["foliaSupport"] as String == "true"
val projectName = properties["name"] as String

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("${gameVersion}-R0.1-SNAPSHOT")

    // Kotlin libraries
    library(kotlin("stdlib"))
    library("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")

    // Minecraft libraries
    library("de.miraculixx:kpaper:1.2.1")
    library("dev.jorel:commandapi-bukkit-shade:9.3.0")
    library("dev.jorel:commandapi-bukkit-kotlin:9.3.0")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

bukkit {
    main = "$group.${projectName.lowercase()}.${projectName}"
    apiVersion = "1.16"
    foliaSupported = foliaSupport

    // Optionals
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    depend = listOf()
    softDepend = listOf()
}

modrinth {
    token.set(properties["modrinthToken"] as String)
    projectId.set(properties["modrinthProjectId"] as? String ?: projectName)
    versionNumber.set(version as String)
    versionType.set("release") // Can also be `beta` or `alpha`
    uploadFile.set(tasks.jar)
    gameVersions.addAll((properties["supportedVersions"] as String).split(','))
    loaders.addAll(buildList {
        add("paper")
        add("purpur")
        if (foliaSupport) add("folia")
    })
    dependencies {
        // The scope can be `required`, `optional`, `incompatible`, or `embedded`
        // The type can either be `project` or `version`
//        required.project("fabric-api")
    }

    // Project sync
    syncBodyFrom = rootProject.file("README.md").readText()
}
