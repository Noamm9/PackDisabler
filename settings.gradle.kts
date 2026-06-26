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
		id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
		id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT"
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.7.5"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"

	create(rootProject) {
		vers("1.21.11", "1.21.11")
		vers("26.1.2", "26.1.2")
		vers("26.2", "26.2")
	}
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "PackDisabler"
