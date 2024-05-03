import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dex.plugins.outlet.v2.util.ReleaseType
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("io.papermc.paperweight.userdev") version "1.7.0"
    id("xyz.jpenilla.run-paper") version "2.2.4"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.modrinth.minotaur") version "2.+"
    id("io.github.dexman545.outlet") version "1.6.1"
    id("io.github.goooler.shadow") version "8.1.7"
}

group = properties["group"] as String
version = properties["version"] as String
description = properties["description"] as String

val gameVersion by properties
val foliaSupport = properties["foliaSupport"] as String == "true"
val projectName = properties["name"] as String
val commandAPIVersion = properties["commandAPIVersion"] as String

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("${gameVersion}-R0.1-SNAPSHOT")

    // Kotlin libraries
    library(kotlin("stdlib"))
    library("org.jetbrains.kotlinx:kotlinx-serialization-json:1.+")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.+")

    // Minecraft libraries
    library("de.miraculixx:kpaper:1.+")
    implementation("dev.jorel:commandapi-bukkit-shade:$commandAPIVersion")
    implementation("dev.jorel:commandapi-bukkit-kotlin:$commandAPIVersion")
}

tasks {
    assemble {
        dependsOn(shadowJar)
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
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
    outlet.mcVersionRange = properties["supportedVersions"] as String
    outlet.allowedReleaseTypes = setOf(ReleaseType.RELEASE)
    gameVersions.addAll(outlet.mcVersions())
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

tasks.withType<ShadowJar> {
    dependencies {
        include(dependency("dev.jorel:commandapi-bukkit-shade:$commandAPIVersion"))
    }
    println("Relocate to $group.$name")
    relocate("dev.jorel.commandapi", "$group.$name.commandapi")
    minimize()
}
