pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.kikugie.dev/snapshots")
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
	centralScript = "build.gradle.kts"

	create(rootProject) {
		versions("1.20.1", "1.20.2", "1.20.4", "1.20.6", "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.9")
		vcsVersion = "1.21.9"
	}
}