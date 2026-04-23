package net.pneumono.gravestones.content;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneDataSaving;
import net.pneumono.gravestones.gravestones.GravestoneHistory;
import net.pneumono.gravestones.gravestones.GravestoneManager;
import net.pneumono.gravestones.multiversion.GraveOwner;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.List;
import java.util.UUID;

//? if >=1.21.11
import net.minecraft.commands.Commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GravestonesCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("gravestones")
                        .requires(/*? if >=1.21.11 {*/Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)/*?} else {*//*source -> source.hasPermission(4)*//*?}*/)
                        .then(literal("getdata")
                                .then(literal("gravestone")
                                        .then(argument("position", BlockPosArgument.blockPos())
                                                .executes(context -> {
                                                    ServerLevel level = context.getSource().getLevel();
                                                    BlockPos pos = BlockPosArgument.getBlockPos(context, "position");

                                                    if (!(level.getBlockState(pos).is(GravestonesRegistry.GRAVESTONE_TECHNICAL))) {
                                                        context.getSource().sendSuccess(() -> Component.translatable("commands.gravestones.getdata.gravestone.no_gravestone").withStyle(ChatFormatting.RED), false);
                                                    } else if (level.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity entity) {
                                                        GraveOwner graveOwner = entity.getGraveOwner();
                                                        if (graveOwner != null) {
                                                            context.getSource().sendSuccess(() -> Component.translatable("commands.gravestones.getdata.gravestone.all_data", entity.getSpawnDateTime(), graveOwner.getNotNullName(), graveOwner.getUuid()).withStyle(ChatFormatting.GREEN), false);
                                                        } else {
                                                            context.getSource().sendSuccess(() -> Component.translatable("commands.gravestones.getdata.gravestone.no_grave_owner", entity.getSpawnDateTime()).withStyle(ChatFormatting.RED), false);
                                                        }

                                                        context.getSource().sendSuccess(() -> Component.translatable("commands.gravestones.getdata.gravestone.contents_data", NbtUtils.toPrettyComponent(entity.getContents())), false);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                                .then(literal("player")
                                        .then(argument("player", EntityArgument.player())
                                                .executes(context -> {
                                                    UUID uuid = VersionUtil.getId(EntityArgument.getPlayer(context, "player").getGameProfile());
                                                    GravestoneHistory history = GravestoneDataSaving.readHistory(context.getSource().getServer(), uuid);
                                                    List<GlobalPos> positions = history.getPositions();

                                                    if (positions == null) {
                                                        Gravestones.LOGGER.error("Could not find gravestone data file!");
                                                        context.getSource().sendSuccess(() -> Component.translatable("commands.gravestones.getdata.player.cannot_find").withStyle(ChatFormatting.RED), false);
                                                        return 0;
                                                    }

                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    MutableComponent text = Component.empty();
                                                    if (!positions.isEmpty()) {
                                                        text.append(GravestoneManager.posToText(positions.get(0)));
                                                        for (int i = 1; i < positions.size(); ++i) {
                                                            text.append(", ");
                                                            text.append(GravestoneManager.posToText(positions.get(i)));
                                                        }
                                                    }
                                                    context.getSource().sendSuccess(() -> Component.translatable("commands.gravestones.getdata.player.grave_data",
                                                            player.getDisplayName(), text
                                                    ), false);

                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(literal("deaths")
                                .then(literal("view")
                                        .then(argument("death", DeathArgumentType.death())
                                                .executes(context -> {
                                                    CommandSourceStack source = context.getSource();

                                                    CompoundTag nbt = DeathArgumentType.getDeath(context, "death");

                                                    source.sendSuccess(() -> Component.translatable("commands.gravestones.deaths.view", NbtUtils.toPrettyComponent(VersionUtil.getCompoundOrEmpty(nbt, "contents"))), false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(literal("recover")
                                        .then(argument("death", DeathArgumentType.death())
                                                .executes(context -> recoverDeath(context, DeathArgumentType.getDeath(context, "death"), context.getSource().getPlayerOrException()))
                                                .then(argument("player", EntityArgument.player())
                                                        .executes(context -> recoverDeath(
                                                                context,
                                                                DeathArgumentType.getDeath(context, "death"),
                                                                EntityArgument.getPlayer(context, "player")
                                                        ))
                                                )
                                        )
                                )
                        )
                        .then(literal("getuuid")
                                .then(argument("player", EntityArgument.player())
                                        .executes(
                                                context -> getUuid(context, EntityArgument.getPlayer(context, "player"))
                                        )
                                )
                                .executes(
                                        context -> getUuid(context, context.getSource().getPlayerOrException())
                                )
                        )
                )
        );
    }

    private static int recoverDeath(CommandContext<CommandSourceStack> context, CompoundTag nbt, ServerPlayer player) {
        GravestonesApi.onCollect(context.getSource().getLevel(), player.blockPosition(), player, 0, VersionUtil.getCompoundOrEmpty(nbt, "contents").copy());
        context.getSource().sendSuccess(() -> Component.translatable("commands.gravestones.deaths.recover"), true);
        return 1;
    }

    private static int getUuid(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        context.getSource().sendSuccess(() -> Component.translatable("commands.gravestones.getuuid", player.getDisplayName(), player.getStringUUID()), false);
        return 1;
    }
}
