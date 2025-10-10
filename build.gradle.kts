plugins {
	id("fabric-loom") version "1.11-SNAPSHOT"
	id("maven-publish")
}

val javaVersion = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5"))
	JavaVersion.VERSION_21 else JavaVersion.VERSION_17
java.targetCompatibility = javaVersion
java.sourceCompatibility = javaVersion

base.archivesName.set(project.property("mod_id") as String)
version = "${project.property("mod_version")}+${stonecutter.current.project}"

val trinkets = "${property("trinkets_version")}" != "[VERSIONED]"
val accessories = "${property("accessories_version")}" != "[VERSIONED]" && "${property("owo_version")}" != "[VERSIONED]"

repositories {
	// Mod Menu
	maven("https://maven.terraformersmc.com/")
	if (stonecutter.current.project == "1.20.4") {
		maven("https://maven.nucleoid.xyz/")
	}

	// Trinkets
	if (trinkets) {
		maven("https://maven.ladysnake.org/releases")
	}

	// Accessories
	if (accessories) {
		maven("https://maven.wispforest.io/releases")
		maven("https://maven.su5ed.dev/releases")
		maven("https://maven.shedaniel.me/")
	}

	// Core
	exclusiveContent {
		forRepository {
			maven("https://api.modrinth.com/maven")
		}
		filter {
			includeGroup("maven.modrinth")
		}
	}
}

loom {
	accessWidenerPath = file("src/main/resources/gravestones.accesswidener")

	splitEnvironmentSourceSets()

	mods {
		create("gravestones") {
			sourceSet(sourceSets["main"])
			sourceSet(sourceSets["client"])
		}
	}

	runConfigs.all {
		ideConfigGenerated(true)
	}
}

stonecutter {
	constants["accessories"] = accessories
	constants["trinkets"] = trinkets
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${stonecutter.current.version}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

	// Core mod
	modImplementation("maven.modrinth:pneumono_core:${property("core_version")}")

	// ModMenu
	modCompileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")
	modRuntimeOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")

	// Accessories
	if (accessories) {
		modCompileOnly("io.wispforest:accessories-fabric:${property("accessories_version")}")
		modCompileOnly("io.wispforest:owo-lib:${property("owo_version")}")
	}

	// Trinkets
	if (trinkets) {
		modCompileOnly("dev.emi:trinkets:${property("trinkets_version")}")
		if (stonecutter.current.project == "1.20.2") {
			modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:5.3.0")
		}
	}
}

tasks {
	processResources {
		inputs.property("version", stonecutter.current.version)
		inputs.property("supported", project.property("supported_versions"))

		filesMatching("fabric.mod.json") {
			expand(
				mutableMapOf(
					"version" to stonecutter.current.version,
					"supported" to project.property("supported_versions")
				)
			)
		}

		val mixins = if (stonecutter.eval(stonecutter.current.version, ">=1.20.4"))
			"LivingEntityMixin" else "ExplosionMixin\", \"LivingEntityMixin"

		filesMatching("gravestones.mixins.json") {
			expand(
				mutableMapOf(
					"mixins" to mixins
				)
			)
		}
	}

	withType<JavaCompile> {
		val java = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5")) 21 else 17
		options.release.set(java)
	}

	java {
		withSourcesJar()
	}

	jar {
		from("LICENSE") {
			rename {"${it}_${base.archivesName.get()}"}
		}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
