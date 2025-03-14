package net.pneumono.gravestones;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.pneumono.gravestones.compat.TrinketsSupport;
import net.pneumono.gravestones.content.GravestonesCommands;
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gravestones implements ModInitializer {
	public static final String MOD_ID = "gravestones";
	public static final Logger LOGGER = LoggerFactory.getLogger("Gravestones");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Gravestones");
		GravestonesConfig.registerConfigs();

		GravestonesRegistry.registerModContent();
		GravestonesCommands.registerCommands();

		// Commented out until Trinkets updates to 1.21.4
		/*
		if (FabricLoader.getInstance().isModLoaded("trinkets")) {
			TrinketsSupport.register();
		}
		 */
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}