plugins {
	id("fabric-loom") version "1.14-SNAPSHOT"
	id("maven-publish")
	id("me.modmuss50.mod-publish-plugin") version "1.0.0"
	id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

val javaVersion = if (stonecutter.eval(stonecutter.current.version, ">=1.20.5"))
	JavaVersion.VERSION_21 else JavaVersion.VERSION_17
java.targetCompatibility = javaVersion
java.sourceCompatibility = javaVersion

val awFile =
	if (stonecutter.eval(stonecutter.current.version, ">=1.21.9"))
		"1.21.9.accesswidener"
	else if (stonecutter.eval(stonecutter.current.version, ">=1.21.4"))
		"1.21.4.accesswidener"
	else if (stonecutter.eval(stonecutter.current.version, ">=1.20.5"))
		"1.20.5.accesswidener"
	else if (stonecutter.eval(stonecutter.current.version, ">=1.20.2"))
		"1.20.2.accesswidener"
	else
		"1.20.accesswidener"

base.archivesName.set(project.property("mod_id") as String)
version = "${project.property("mod_version")}+${stonecutter.current.project}+${property("mod_subversion")}"

val galosphere = "${property("galosphere_version")}" != "[VERSIONED]"
val resourceBackpacks = "${property("resource_backpacks_version")}" != "[VERSIONED]"
val trinkets = "${property("trinkets_version")}" != "[VERSIONED]"
val trinketsCanary = "${property("trinkets_canary_version")}" != "[VERSIONED]"
val accessories = "${property("accessories_version")}" != "[VERSIONED]" && "${property("owo_version")}" != "[VERSIONED]"

repositories {
	// Mod Menu
	maven("https://maven.terraformersmc.com/")
	if (stonecutter.current.project == "1.20.3") {
		maven("https://maven.nucleoid.xyz/")
	}

	// Trinkets & Trinkets Canary
	if (trinkets || trinketsCanary) {
		maven("https://maven.ladysnake.org/releases")
	}

	// Accessories
	if (accessories) {
		maven("https://maven.wispforest.io/releases")
		maven("https://maven.su5ed.dev/releases")
		maven("https://maven.shedaniel.me/")
	}

	// Core, Trinkets Canary, Galosphere, Resource Backpacks
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
	accessWidenerPath = rootProject.file("src/main/resources/access_wideners/$awFile")

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
	constants["galosphere"] = galosphere
	constants["resource_backpacks"] = resourceBackpacks
	constants["accessories"] = accessories
	constants["trinkets"] = trinkets || trinketsCanary
}

fletchingTable {

	j52j.register("main") {
		if (stonecutter.eval(stonecutter.current.version, ">=1.21")) {
			extension("json", "data/**/*.json5")
		} else {
			extension("json", "data/gravestones/advancement/recipes/decorations/*.json5 -> /data/gravestones/advancements/recipes/decorations")
			extension("json", "data/gravestones/loot_table/blocks/*.json5 -> /data/gravestones/loot_tables/blocks")
			extension("json", "data/gravestones/recipe/*.json5 -> ../recipes")
			extension("json", "data/gravestones/tags/block/*.json -> ../blocks")
			extension("json", "data/gravestones/tags/enchantment/*.json -> ../enchantments")
			extension("json", "data/gravestones/tags/item/*.json -> ../items")
			extension("json", "data/minecraft/tags/block/*.json -> ../blocks")
			extension("json", "data/minecraft/tags/block/mineable/*.json -> /data/minecraft/tags/blocks/mineable")
		}
	}
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
	modImplementation("com.terraformersmc:modmenu:${property("modmenu_version")}")

	// Galosphere
	if (galosphere) {
		modCompileOnly("maven.modrinth:galosphere:${property("galosphere_version")}")
	}

	// Resource Backpacks
	if (resourceBackpacks) {
		modCompileOnly("maven.modrinth:resource-backpacks:${property("resource_backpacks_version")}")
	}

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
	if (trinketsCanary) {
		modCompileOnly("maven.modrinth:trinkets-canary:${property("trinkets_canary_version")}")
		val ccaVersion = when (stonecutter.current.project) {
			"1.21.4" -> "6.2.2"
			"1.21.5" -> "6.3.1"
			"1.21.6" -> "7.0.0-beta.1"
			"1.21.9" -> "7.2.0"
			"1.21.11" -> "7.3.0"
			else -> "null"
		}
		if (ccaVersion != "null") {
			modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${ccaVersion}")
			modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${ccaVersion}")
		}
	}
}

tasks {
	processResources {
		inputs.property("version", project.property("mod_version"))
		inputs.property("min_supported", project.property("min_supported_version"))
		inputs.property("max_supported", project.property("max_supported_version"))

		filesMatching("fabric.mod.json") {
			expand(
				mutableMapOf(
					"version" to project.property("mod_version"),
					"min_supported" to project.property("min_supported_version"),
					"max_supported" to project.property("max_supported_version"),
					"aw_file" to awFile
				)
			)
		}

		val mixins = if (stonecutter.eval(stonecutter.current.version, ">=1.20.3"))
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

publishMods {
	file = tasks.remapJar.get().archiveFile
	additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)
	displayName = "Gravestones ${project.version}"
	version = "${project.version}"
	changelog = rootProject.file("CHANGELOG.md").readText()
	type = STABLE
	modLoaders.addAll("fabric", "quilt")

	dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

	modrinth {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectId = "Heh3BbSv"

		minecraftVersionRange {
			start = "${property("min_supported_version")}"
			end = "${property("max_supported_version")}"
		}

		requires {
			// PneumonoCore
			id = "ZLKQjA7t"
		}

		requires {
			// Fabric API
			id = "P7dR8mSH"
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
