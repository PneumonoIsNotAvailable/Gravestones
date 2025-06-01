package net.pneumono.gravestones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.compat.SpelunkerySupport;
import net.pneumono.gravestones.compat.TrinketsSupport;
import net.pneumono.gravestones.content.GravestonesCommands;
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Function;

public class Gravestones implements ModInitializer {
	public static final String MOD_ID = "gravestones";
	public static final Function<MinecraftServer, File> GRAVESTONES_ROOT = server -> new File(server.getSavePath(WorldSavePath.ROOT).toString(), "gravestones");
	public static final Logger LOGGER = LoggerFactory.getLogger("Gravestones");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Gravestones");
		GravestonesConfig.registerConfigs();

		GravestonesRegistry.registerModContent();
		GravestonesCommands.registerCommands();

		// Commented out until Trinkets updates to 1.21.5
		/*
		if (FabricLoader.getInstance().isModLoaded("trinkets")) {
			TrinketsSupport.register();
		}
		 */
		if (FabricLoader.getInstance().isModLoaded("spelunkery")) {
			GravestonesApi.registerModSupport(new SpelunkerySupport());
		}
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}