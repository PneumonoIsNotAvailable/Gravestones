package net.pneumono.gravestones.content;

import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.data.GravestoneData;
import net.pneumono.gravestones.gravestones.data.GravestonePosition;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

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
                                                        context.getSource().sendMessage(Text.literal("No gravestone at that position!").formatted(Formatting.RED));
                                                    } else if (world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity entity) {
                                                        ProfileComponent owner = entity.getGraveOwner();
                                                        if (owner != null) {
                                                            context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDateTime() + " and a graveOwner of " + owner.name().orElse("???") + " (" + owner.id().orElse(null) + ")").formatted(Formatting.GREEN));
                                                        } else {
                                                            context.getSource().sendMessage(Text.literal("Gravestone has a spawnDate of " + entity.getSpawnDateTime() + " and no graveOwner!").formatted(Formatting.RED));
                                                        }

                                                        String inventoryMessage = getInventoryMessage(entity);

                                                        context.getSource().sendMessage(Text.literal("Gravestone has the following items " + inventoryMessage).formatted(Formatting.GOLD));
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
                                                            Gravestones.LOGGER.error("Could not find gravestone data file.");
                                                            context.getSource().sendMessage(Text.literal("Could not find gravestone data file.").formatted(Formatting.RED));
                                                        }
                                                    } catch (IOException e) {
                                                        Gravestones.LOGGER.error("Could not read gravestone data file!", e);
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

    private static String getInventoryMessage(TechnicalGravestoneBlockEntity entity) {
        StringBuilder inventoryMessage = new StringBuilder();
        boolean notFirst = false;
        for (ItemStack item : entity.getItems()) {
            if (notFirst) {
                inventoryMessage.append(", ");
            } else {
                notFirst = true;
            }
            inventoryMessage.append(item.toString());
        }
        return inventoryMessage.toString();
    }
}
