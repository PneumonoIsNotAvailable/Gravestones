package net.pneumono.gravestones;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.compat.CompatRegistry;
import net.pneumono.gravestones.compat.TrinketsSupport;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.*;
import net.pneumono.pneumonocore.config.*;
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

	public static final BooleanConfiguration AESTHETIC_GRAVESTONES = Configs.register(new BooleanConfiguration(MOD_ID, "aesthetic_gravestones", ConfigEnv.SERVER, true));
	public static final BooleanConfiguration DECAY_WITH_TIME = Configs.register(new BooleanConfiguration(MOD_ID, "decay_with_time", ConfigEnv.SERVER, true));
	public static final BooleanConfiguration DECAY_WITH_DEATHS = Configs.register(new BooleanConfiguration(MOD_ID, "decay_with_deaths", ConfigEnv.SERVER, true));
	public static final TimeConfiguration DECAY_TIME = Configs.register(new TimeConfiguration(MOD_ID, "decay_time", ConfigEnv.SERVER, 8L * TimeUnit.HOURS.getDivision()));
	public static final EnumConfiguration<DecayTimeType> GRAVESTONE_DECAY_TIME_TYPE = Configs.register(new EnumConfiguration<>(MOD_ID, "decay_time_type", ConfigEnv.SERVER, DecayTimeType.TICKS));
	public static final BooleanConfiguration STORE_EXPERIENCE = Configs.register(new BooleanConfiguration(MOD_ID, "store_experience", ConfigEnv.SERVER, true));
	public static final BooleanConfiguration EXPERIENCE_CAP = Configs.register(new BooleanConfiguration(MOD_ID, "experience_cap", ConfigEnv.SERVER, true));
	public static final EnumConfiguration<ExperienceKeptCalculation> EXPERIENCE_KEPT = Configs.register(new EnumConfiguration<>(MOD_ID, "experience_kept", ConfigEnv.SERVER, ExperienceKeptCalculation.VANILLA));
	public static final BooleanConfiguration EXPERIENCE_DECAY = Configs.register(new BooleanConfiguration(MOD_ID, "experience_decay", ConfigEnv.SERVER, false));
	public static final BooleanConfiguration GRAVESTONE_ACCESSIBLE_OWNER_ONLY = Configs.register(new BooleanConfiguration(MOD_ID, "gravestone_accessible_owner_only", ConfigEnv.SERVER, true));
	public static final BooleanConfiguration SPAWN_GRAVESTONE_SKELETONS = Configs.register(new BooleanConfiguration(MOD_ID, "spawn_gravestone_skeletons", ConfigEnv.SERVER, false));
	public static final BooleanConfiguration BROADCAST_COLLECT_IN_CHAT = Configs.register(new BooleanConfiguration(MOD_ID, "broadcast_collect_in_chat", ConfigEnv.SERVER, false));
	public static final BooleanConfiguration BROADCAST_COORDINATES_IN_CHAT = Configs.register(new BooleanConfiguration(MOD_ID, "broadcast_coordinates_in_chat", ConfigEnv.SERVER, false));
	public static final BooleanConfiguration CONSOLE_INFO = Configs.register(new BooleanConfiguration(MOD_ID, "console_info", ConfigEnv.CLIENT, false));
	public static final EnumConfiguration<TimeFormatType> TIME_FORMAT = Configs.register(new EnumConfiguration<>(MOD_ID, "time_format", ConfigEnv.CLIENT, TimeFormatType.MMDDYYYY));

	public static final Identifier RESOURCE_CONDITION_CONFIGURATIONS = identifier("configurations");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Gravestones");
		Configs.reload(MOD_ID);

		GravestonesRegistry.registerModContent();
		registerCommands();

		ResourceConditions.register(RESOURCE_CONDITION_CONFIGURATIONS, jsonObject -> AESTHETIC_GRAVESTONES.getValue());

		CompatRegistry.registerCompat();
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
									GameProfile owner = entity.getGraveOwner();
									if (owner != null) {
										context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDateTime() + " and a graveOwner of " + owner.getName() + " (" + owner.getId() + ")").formatted(Formatting.GREEN));
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