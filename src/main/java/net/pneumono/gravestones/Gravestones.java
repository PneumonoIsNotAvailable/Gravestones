package net.pneumono.gravestones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.pneumono.gravestones.api.SkipItemCallback;
import net.pneumono.gravestones.compat.AccessoriesCompat;
import net.pneumono.gravestones.compat.BackwardsCompat;
import net.pneumono.gravestones.compat.TrinketsCompat;
import net.pneumono.gravestones.content.GravestonesCommands;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.gravestones.GravestoneDataSaving;
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

		ServerLifecycleEvents.SERVER_STARTED.register(BackwardsCompat::convertOldFiles);

		GravestonesRegistry.registerModContent();
		GravestonesCommands.registerCommands();

		if (isModLoaded("spelunkery")) {
			SkipItemCallback.EVENT.register((player, itemStack, slot) -> itemStack.isOf(Items.RECOVERY_COMPASS));
		}

		// Accessories' Compat Layers exist, so to prevent issues Gravestones will prioritize Accessories directly over other mods
		boolean usingAccessories = false;

		//? if accessories {
		/*if (isModLoaded("accessories")) {
			AccessoriesCompat.register();
			usingAccessories = true;
		}
		*///?}

		//? if trinkets {
		/*if (!usingAccessories && isModLoaded("trinkets")) {
			TrinketsCompat.register();
		}
		*///?}
	}

	private static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}