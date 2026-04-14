pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.kikugie.dev/snapshots") {
			name = "KikuGie Snapshots"
		}
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
	create(rootProject) {
		fun controlledVersions(vararg versions: String) = versions.forEach {
			if (stonecutter.eval(it, ">=26.1")) {
				version(it).buildscript = "build.noremap.gradle.kts"
			} else {
				version(it).buildscript = "build.remap.gradle.kts"
			}
		}

		controlledVersions("1.20", "1.20.2", "1.20.3", "1.20.5", "1.21", "1.21.2", "1.21.4", "1.21.5", "1.21.6", "1.21.9", "1.21.11", "26.1")
		vcsVersion = "26.1"
	}
}