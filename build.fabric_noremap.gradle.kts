plugins {
	id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
	id("maven-publish")
	id("me.modmuss50.mod-publish-plugin") version "1.0.0"
	id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

val javaVersion = JavaVersion.VERSION_25
java.targetCompatibility = javaVersion
java.sourceCompatibility = javaVersion

val awFile = "26.1.accesswidener"
base.archivesName.set(project.property("mod_id") as String)
version = "${project.property("mod_version")}+${stonecutter.current.project}+${property("mod_subversion")}"

val galosphere = "${property("galosphere_version")}" != "[VERSIONED]"
val resourceBackpacks = "${property("resource_backpacks_version")}" != "[VERSIONED]"
val backpacked = "${property("backpacked_version")}" != "[VERSIONED]"
val trinkets = "${property("trinkets_version")}" != "[VERSIONED]"
val trinketsCanary = "${property("trinkets_canary_version")}" != "[VERSIONED]"
val accessories = "${property("accessories_version")}" != "[VERSIONED]" && "${property("owo_version")}" != "[VERSIONED]"

repositories {
	// Mod Menu
	maven("https://maven.terraformersmc.com/")

	exclusiveContent {
		forRepository {
			maven("https://cursemaven.com")
		}
		filter {
			includeGroup("curse.maven")
		}
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
	constants["backpacked"] = backpacked
	constants["accessories"] = accessories
	constants["trinkets"] = trinkets || trinketsCanary
}

fletchingTable {
	j52j.register("main") {
		extension("json", "data/**/*.json5")
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${stonecutter.current.version}")
	implementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

	// Core mod
	implementation("maven.modrinth:pneumono_core:${property("core_version")}")

	// ModMenu
	runtimeOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")

	// Galosphere
	if (galosphere) {
		compileOnly("maven.modrinth:galosphere:${property("galosphere_version")}")
	}

	// Resource Backpacks
	if (resourceBackpacks) {
		compileOnly("maven.modrinth:resource-backpacks:${property("resource_backpacks_version")}")
	}

	// Backpacked
	if (backpacked) {
		compileOnly("curse.maven:backpacked-352835:${property("backpacked_version")}")
	}

	// Accessories
	if (accessories) {
		compileOnly("io.wispforest:accessories-fabric:${property("accessories_version")}")
		compileOnly("io.wispforest:owo-lib:${property("owo_version")}")
	}

	// Trinkets
	if (trinkets) {
		compileOnly("dev.emi:trinkets:${property("trinkets_version")}")
		if (stonecutter.current.project == "1.20.2") {
			compileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:5.3.0")
		}
	}
	if (trinketsCanary) {
		compileOnly("maven.modrinth:trinkets-canary:${property("trinkets_canary_version")}")
		val ccaVersion = when (stonecutter.current.project) {
			"1.21.4" -> "6.2.2"
			"1.21.5" -> "6.3.1"
			"1.21.6" -> "7.0.0-beta.1"
			"1.21.9" -> "7.2.0"
			"1.21.11" -> "7.3.0"
			else -> "null"
		}
		if (ccaVersion != "null") {
			compileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${ccaVersion}")
			compileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${ccaVersion}")
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

		val mixins = "LivingEntityMixin"

		filesMatching("gravestones.mixins.json") {
			expand(
				mutableMapOf(
					"mixins" to mixins
				)
			)
		}
	}

	withType<JavaCompile> {
		val java = 25
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

stonecutter {
	replacements.string {
		direction = eval(current.version, ">=1.21.11")
		replace("ResourceLocation", "Identifier")
	}
}

publishMods {
	file = tasks.jar.map { it.archiveFile.get() }
	additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile.get() })
	displayName = "Gravestones ${project.version}"
	version = "${project.version}"
	changelog = rootProject.file("CHANGELOG.md").readText()
	type = STABLE
	modLoaders.addAll("fabric", "quilt")

	val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN")
	val discordToken = providers.environmentVariable("DISCORD_TOKEN")

	dryRun = modrinthToken.getOrNull() == null || discordToken.getOrNull() == null

	modrinth {
		accessToken = modrinthToken
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

	if (stonecutter.current.project == "26.1") {
		discord {
			webhookUrl = discordToken

			username = "Gravestones Updates"

			avatarUrl = "https://github.com/PneumonoIsNotAvailable/Gravestones/blob/master/src/main/resources/assets/gravestones/icon.png?raw=true"

			content = changelog.map { "# Gravestones version ${project.property("mod_version")}\n<@&1472490332783378472>\n" + it }
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
