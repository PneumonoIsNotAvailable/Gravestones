package net.pneumono.gravestones;

import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.compat.TrinketsSupport;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Gravestones implements ModInitializer {
	public static final String MOD_ID = "gravestones";
	public static final Logger LOGGER = LoggerFactory.getLogger("Gravestones");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Gravestones");
		GravestonesConfig.registerConfigs();

		GravestonesRegistry.registerModContent();
		registerCommands();

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

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(literal("gravestones")
				.requires(source -> source.hasPermissionLevel(4))
				.then(literal("getdata")
					.then(literal("gravestone")
						.then(argument("position", BlockPosArgumentType.blockPos())
							.executes(context -> {
								ServerWorld world = context.getSource().getWorld();
								BlockPos pos = BlockPosArgumentType.getBlockPos(context, "position");

								if (!(world.getBlockState(pos).isOf(GravestonesRegistry.GRAVESTONE_TECHNICAL))) {
									context.getSource().sendMessage(Text.literal("No gravestone at that position!").formatted(Formatting.RED));
								} else if (world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity entity) {
									ProfileComponent owner = entity.getGraveOwner();
									if (owner != null) {
										context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDateTime() + " and a graveOwner of " + owner.name().orElse("???") + " (" + owner.id().orElse(null) + ")").formatted(Formatting.GREEN));
									} else {
										context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDateTime() + " and no graveOwner!").formatted(Formatting.RED));
									}

									StringBuilder itemMessage = new StringBuilder();
									boolean notFirst = false;
									for (ItemStack item : entity.getItems()) {
										if (notFirst) {
											itemMessage.append(", ");
										} else {
											notFirst = true;
										}
										itemMessage.append(item.toString());
									}

									context.getSource().sendMessage(Text.literal("Gravestone has the following items " + itemMessage).formatted(Formatting.GOLD));
									context.getSource().sendMessage(Text.literal("Gravestone has " + entity.getExperience() + " experience points").formatted(Formatting.GOLD));
									context.getSource().sendMessage(Text.literal("Gravestone has the following mod data " + entity.getAllModData().toString()).formatted(Formatting.GOLD));
								}
								return 1;
							})
						)
					)
					.then(literal("player")
						.then(argument("player", EntityArgumentType.player())
							.executes(context -> {
								File gravestoneFile = new File(context.getSource().getWorld().getServer().getSavePath(WorldSavePath.ROOT).toString(), "gravestone_data.json");

								try {
									if (gravestoneFile.exists()) {
										Reader reader = Files.newBufferedReader(gravestoneFile.toPath());
										GravestoneData data = new GsonBuilder().serializeNulls().setPrettyPrinting().create().fromJson(reader, GravestoneData.class);
										reader.close();

										List<GravestonePosition> positions = data.getPlayerGravePositions(EntityArgumentType.getPlayer(context, "player").getGameProfile().getId());
										StringBuilder posList = new StringBuilder();
										boolean notFirst = false;
										for (GravestonePosition pos : positions) {
											if (notFirst) {
												posList.append(", ");
											} else {
												notFirst = true;
											}
											posList.append("(").append(pos.posX).append(",").append(pos.posY).append(",").append(pos.posZ).append(") in ").append(pos.dimension);
										}
										context.getSource().sendMessage(Text.literal(Objects.requireNonNull(EntityArgumentType.getPlayer(context, "player").getDisplayName()).getString() + " has graves at the following locations: " + posList));
									} else {
										LOGGER.error("Could not find gravestone data file.");
										context.getSource().sendMessage(Text.literal("Could not find gravestone data file.").formatted(Formatting.RED));
									}
								} catch (IOException e) {
									LOGGER.error("Could not read gravestone data file!", e);
									context.getSource().sendMessage(Text.literal("Could not read gravestone data file!").formatted(Formatting.RED));
								}

								return 1;
							})
						)
					)
				)
			)
		);
	}
}