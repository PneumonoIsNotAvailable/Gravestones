package net.pneumono.gravestones.content;

import com.google.gson.GsonBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.data.GravestoneData;
import net.pneumono.gravestones.gravestones.data.GravestonePosition;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GravestonesCommands {
    public static void registerCommands() {
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
                                                        context.getSource().sendFeedback(() -> Text.translatable("commands.gravestones.getdata.gravestone.no_gravestone").formatted(Formatting.RED), false);
                                                    } else if (world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity entity) {
                                                        ProfileComponent owner = entity.getGraveOwner();
                                                        if (owner != null) {
                                                            context.getSource().sendFeedback(() -> Text.stringifiedTranslatable("commands.gravestones.getdata.gravestone.all_data", entity.getSpawnDateTime(), owner.name().orElse("???"), owner.uuid().orElse(null)).formatted(Formatting.GREEN), false);
                                                        } else {
                                                            context.getSource().sendFeedback(() -> Text.translatable("commands.gravestones.getdata.gravestone.no_grave_owner", entity.getSpawnDateTime()).formatted(Formatting.RED), false);
                                                        }

                                                        context.getSource().sendFeedback(() -> Text.translatable("commands.gravestones.getdata.gravestone.contents_data", NbtHelper.toPrettyPrintedText(entity.getContents())), false);
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
                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                            context.getSource().sendFeedback(() -> Text.translatable("commands.gravestones.getdata.player.grave_data", player.getDisplayName(), positions.getFirst().toText(), positions.get(1).toText(), positions.get(2).toText()), false);
                                                        } else {
                                                            Gravestones.LOGGER.error("Could not find gravestone data file!");
                                                            context.getSource().sendFeedback(() -> Text.translatable("commands.gravestones.getdata.player.cannot_find").formatted(Formatting.RED), false);
                                                        }
                                                    } catch (IOException e) {
                                                        Gravestones.LOGGER.error("Could not read gravestone data file!", e);
                                                        context.getSource().sendFeedback(() -> Text.translatable("commands.gravestones.getdata.player.cannot_read").formatted(Formatting.RED), false);
                                                    }

                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(literal("deaths")
                                .then(literal("view")
                                        .then(argument("death", DeathArgumentType.death())
                                                .executes(context -> {
                                                    ServerCommandSource source = context.getSource();

                                                    NbtCompound nbt = DeathArgumentType.getDeath(context, "death");

                                                    source.sendFeedback(() -> Text.translatable("commands.gravestones.deaths.view", NbtHelper.toPrettyPrintedText(nbt.getCompoundOrEmpty("contents"))), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(literal("recover")
                                        .then(argument("death", DeathArgumentType.death())
                                                .executes(context -> recoverDeath(context, DeathArgumentType.getDeath(context, "death"), context.getSource().getPlayerOrThrow()))
                                                .then(argument("player", EntityArgumentType.player())
                                                        .executes(context -> recoverDeath(
                                                                context,
                                                                DeathArgumentType.getDeath(context, "death"),
                                                                EntityArgumentType.getPlayer(context, "player")
                                                        ))
                                                )
                                        )
                                )
                        )
                        .then(literal("getuuid")
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(
                                                context -> getUuid(context, EntityArgumentType.getPlayer(context, "player"))
                                        )
                                )
                                .executes(
                                        context -> getUuid(context, context.getSource().getPlayerOrThrow())
                                )
                        )
                )
        );
    }

    private static int recoverDeath(CommandContext<ServerCommandSource> context, NbtCompound nbt, ServerPlayerEntity player) {
        GravestonesApi.onCollect(player, 0, nbt.getCompoundOrEmpty("contents"));
        context.getSource().sendFeedback(() -> Text.translatable("commands.gravestones.deaths.recover"), true);
        return 1;
    }

    private static int getUuid(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        context.getSource().sendFeedback(() -> Text.translatable("commands.gravestones.getuuid", player.getDisplayName(), player.getUuidAsString()), false);
        return 1;
    }
}
