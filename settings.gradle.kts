pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
        maven("https://repo.polyfrost.cc/releases")
    }

    plugins {
        id("org.jetbrains.kotlin.jvm") version providers.gradleProperty("deps.kotlin_version").get()
        id("net.fabricmc.fabric-loom") version providers.gradleProperty("deps.loom_version").get()
        id("net.kyori.blossom") version providers.gradleProperty("deps.blossom_version").get()
        id("com.google.devtools.ksp") version providers.gradleProperty("deps.ksp_version").get()
        id("dev.kikugie.fletching-table.fabric") version providers.gradleProperty("deps.fletchingtable_version").get()
        id("dev.kikugie.stonecutter") version providers.gradleProperty("deps.stonecutter_version").get()
    }
}

plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter {
    create(rootProject) {
        versions("26.1.2", "26.2", "26.3")
        vcsVersion = "26.3"
    }
}

rootProject.name = "PackDisabler"