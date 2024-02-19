package net.pneumono.gravestones;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.content.entity.GravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.DecayTimeType;
import net.pneumono.gravestones.gravestones.GravestoneData;
import net.pneumono.gravestones.gravestones.GravestonePosition;
import net.pneumono.pneumonocore.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Gravestones implements ModInitializer {
	public static final String MOD_ID = "gravestones";
	public static final Logger LOGGER = LoggerFactory.getLogger("Gravestones");

	public static final BooleanConfiguration AESTHETIC_GRAVESTONES = Configs.register(new BooleanConfiguration(MOD_ID, "aesthetic_gravestones", ConfigEnv.SERVER, true));
	public static final BooleanConfiguration GRAVESTONES_DECAY_WITH_TIME = Configs.register(new BooleanConfiguration(MOD_ID, "gravestones_decay_with_time", ConfigEnv.SERVER, true));
	public static final BooleanConfiguration GRAVESTONES_DECAY_WITH_DEATHS = Configs.register(new BooleanConfiguration(MOD_ID, "gravestones_decay_with_deaths", ConfigEnv.SERVER, true));
	public static final IntegerConfiguration GRAVESTONE_DECAY_TIME_HOURS = Configs.register(new IntegerConfiguration(MOD_ID, "decay_time_hours", ConfigEnv.SERVER, 0, 100, 8));
	public static final EnumConfiguration<DecayTimeType> GRAVESTONE_DECAY_TIME_TYPE = Configs.register(new EnumConfiguration<>(MOD_ID, "decay_time_type", ConfigEnv.SERVER, DecayTimeType.REAL_TIME));
	public static final BooleanConfiguration GRAVESTONE_ACCESSIBLE_OWNER_ONLY = Configs.register(new BooleanConfiguration(MOD_ID, "gravestone_accessible_owner_only", ConfigEnv.SERVER, true));
	public static final BooleanConfiguration SPAWN_GRAVESTONE_SKELETONS = Configs.register(new BooleanConfiguration(MOD_ID, "spawn_gravestone_skeletons", ConfigEnv.SERVER, false));
	public static final BooleanConfiguration BROADCAST_COLLECT_IN_CHAT = Configs.register(new BooleanConfiguration(MOD_ID, "broadcast_collect_in_chat", ConfigEnv.SERVER, false));
	public static final BooleanConfiguration BROADCAST_COORDINATES_IN_CHAT = Configs.register(new BooleanConfiguration(MOD_ID, "broadcast_coordinates_in_chat", ConfigEnv.SERVER, false));
	public static final BooleanConfiguration CONSOLE_INFO = Configs.register(new BooleanConfiguration(MOD_ID, "console_info", ConfigEnv.CLIENT, false));
	public static final StringConfiguration TIME_FORMAT = Configs.register(new StringConfiguration(MOD_ID, "time_format", ConfigEnv.CLIENT, "MM/dd/yyyy HH:mm:ss"));

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Gravestones");
		Configs.reload(MOD_ID);

		GravestonesRegistry.registerModContent();
		registerCommands();
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(literal("gravestones")
				.requires(source -> source.hasPermissionLevel(4))
				.then(literal("getdata")
					.then(literal("gravestone")
						.then(argument("position", BlockPosArgumentType.blockPos())
							.executes(context -> {
								World world = context.getSource().getWorld();
								BlockPos pos = BlockPosArgumentType.getBlockPos(context, "position");

								if (!(world.getBlockState(pos).isOf(GravestonesRegistry.GRAVESTONE_TECHNICAL))) {
									context.getSource().sendMessage(Text.literal("No gravestone at that position!").formatted(Formatting.RED));
								} else if (world.getBlockEntity(pos) instanceof GravestoneBlockEntity entity){
									GameProfile owner = entity.getGraveOwner();
									if (owner != null) {
										context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDateTime() + " and a graveOwner of " + owner.getName() + ", " + owner.getId().toString()).formatted(Formatting.GREEN));
									} else {
										context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDateTime() + " but no graveOwner!").formatted(Formatting.RED));
									}

									StringBuilder itemMessage = new StringBuilder();
									for (ItemStack item : entity.getInventoryAsItemList()) {
										itemMessage.append(", ").append(item.toString());
									}

									context.getSource().sendMessage(Text.literal("Gravestone has the following items" + itemMessage).formatted(Formatting.GOLD));
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

										List<GravestonePosition> positions = data.getPlayerGravePositions(EntityArgumentType.getPlayer(context, "player").getUuid());
										StringBuilder posList = new StringBuilder();
										for (GravestonePosition pos : positions) {
											posList.append("(").append(pos.posX).append(",").append(pos.posY).append(",").append(pos.posZ).append(") in ").append(pos.dimension.toString()).append(", ");
										}
										context.getSource().sendMessage(Text.literal("" + EntityArgumentType.getPlayer(context, "player").getDisplayName().getString() + " has graves at the following locations: " + posList));
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