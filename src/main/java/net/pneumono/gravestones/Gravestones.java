package net.pneumono.gravestones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.pneumono.gravestones.compat.*;
import net.pneumono.gravestones.content.GravestonesApiUsages;
import net.pneumono.gravestones.content.GravestonesCommands;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.gravestones.GravestoneDataSaving;
import net.pneumono.gravestones.multiversion.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Function;

public class Gravestones implements ModInitializer {
	public static final String MOD_ID = "gravestones";
	public static final Function<MinecraftServer, File> GRAVESTONES_ROOT = GravestoneDataSaving::getOrCreateGravestonesFolder;
	public static final Logger LOGGER = LoggerFactory.getLogger("Gravestones");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Gravestones");
		GravestonesConfig.registerGravestonesConfigs();
		if (GravestonesConfig.CONSOLE_INFO.getValue()) {
			LOGGER.info("Gravestones initialized with 'Console Info' config enabled. Disable it if you don't want to see a lot of debug messages here!");
		}

		ServerLifecycleEvents.SERVER_STARTED.register(BackwardsCompat::convertOldFiles);

		GravestonesRegistry.registerModContent();
		GravestonesApiUsages.register();
		GravestonesCommands.registerCommands();

		if (isModLoaded("spelunkery")) {
			SpelunkeryCompat.register();
		}

		if (isModLoaded("galosphere")) {
			GalosphereCompat.register();
		}

		if (isModLoaded("resource_backpacks")) {
			ResourceBackpacksCompat.register();
		}

		if (isModLoaded("backpacked")) {
			BackpackedCompat.register();
		}

		// Accessories' Compat Layers exist, so to prevent issues Gravestones will prioritize Accessories directly over other mods
		boolean usingAccessories = false;

		if (isModLoaded("accessories")) {
			AccessoriesCompat.register();
			usingAccessories = true;
		}

		if (!usingAccessories && isModLoaded("trinkets")) {
			TrinketsCompat.register();
		}
	}

	private static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

	public static Identifier id(String path) {
		return VersionUtil.createId(MOD_ID, path);
	}
}