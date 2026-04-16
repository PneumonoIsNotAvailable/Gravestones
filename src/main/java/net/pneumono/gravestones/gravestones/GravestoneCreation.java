package net.pneumono.gravestones.gravestones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.dimension.DimensionType;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.pneumono.gravestones.api.GravestonePlacedCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.multiversion.GraveOwner;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GravestoneCreation extends GravestoneManager {
    public static void create(Player player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        checkConsoleInfoConfig();

        info("----- Beginning Gravestone Creation -----");
        info("If you don't want to see this, disable 'Console Info' in the configs!");
        create(serverLevel, player);
        info("----- Finishing Gravestone Creation -----");
    }

    private static void create(ServerLevel deathLevel, Player player) {
        GlobalPos deathPos = VersionUtil.createGlobalPos(deathLevel.dimension(), player.blockPosition());
        MinecraftServer server = deathLevel.getServer();

        // Check if placement should be cancelled
        info("Checking if Gravestone Placement should be cancelled...");
        if (CancelGravestonePlacementCallback.EVENT.invoker().shouldCancel(
                deathLevel, player, deathPos
        )) {
            info("Placement cancelled!");
            return;
        }
        info("Placement not cancelled");

        // Read gravestone history
        UUID uuid = player.getUUID();
        CompletableFuture<GravestoneHistory> historyFuture = CompletableFuture.supplyAsync(() -> {
            info("Reading gravestone history...");
            return GravestoneDataSaving.readHistory(server, uuid);
        });

        // Calculate placement position
        info("Calculating gravestone placement position...");
        GlobalPos globalGravestonePos = getPlacementPos(deathLevel, player, deathPos);

        String playerName = VersionUtil.getName(player.getGameProfile());
        if (globalGravestonePos == null || !((Level)(server.getLevel(globalGravestonePos.dimension())) instanceof ServerLevel graveLevel)) {
            Gravestones.LOGGER.info("Failed to place {}'s Gravestone! The items have been dropped on the ground", playerName);
            return;
        }

        // Add new gravestone position to gravestone history
        historyFuture = historyFuture.thenApplyAsync(history -> {
            info("Updating gravestone history...");
            history.getPositions().add(globalGravestonePos);

            return history;
        });

        // Damage existing gravestones
        info("Damaging existing gravestones... (if enabled)");
        GravestoneHistory history = new GravestoneHistory();
        List<GlobalPos> positionsToRemove = new ArrayList<>();
        try {
            history = historyFuture.get();
            List<GlobalPos> positionsToCheck = new ArrayList<>(history.getPositions());
            positionsToCheck.remove(globalGravestonePos);
            positionsToRemove.addAll(GravestoneDecay.deathDamageOldGravestones(server, positionsToCheck));
        } catch (ExecutionException | InterruptedException e) {
            error("Failed to damage existing gravestones", e);
        }

        // Remove broken/non-existent gravestones from history
        for (GlobalPos brokenPos : positionsToRemove) {
            history.getPositions().remove(brokenPos);
        }

        // Write new gravestone history
        GravestoneHistory finalHistory = history;
        CompletableFuture.runAsync(() -> {
            info("Writing updated gravestone history...");
            GravestoneDataSaving.writeHistory(server, uuid, finalHistory);
        });

        // Create contents data
        info("Creating gravestone contents data...");
        CompoundTag contents = createContentsData(player);
        // Backup contents data
        CompletableFuture.runAsync(() -> {
            info("Backing up gravestone contents data...");
            GravestoneDataSaving.saveBackup(contents, player);
        });

        // Place gravestone
        info("Placing gravestone...");
        placeGravestone(graveLevel, globalGravestonePos.pos());
        Gravestones.LOGGER.info("Placed {}'s Gravestone at {}", playerName, posToString(globalGravestonePos));

        // Insert gravestone contents
        info("Inserting contents into gravestone...");
        BlockPos gravestonePos = globalGravestonePos.pos();
        insertGravestoneContents(graveLevel, player, gravestonePos, contents);

        // Broadcast chat message
        info("Broadcasting chat message... (if enabled)");
        if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
            server.getPlayerList().broadcastSystemMessage(Component.translatable("gravestones.grave_spawned", playerName, posToString(gravestonePos)), false);
        }

        // Callbacks
        info("Invoking GravestonePlacedCallbacks...");
        GravestonePlacedCallback.EVENT.invoker().afterGravestonePlace(deathLevel, player, deathPos, globalGravestonePos);
    }

    private static CompoundTag createContentsData(Player player) {
        CompoundTag contents;
        try {
            contents = GravestonesApi.getDataToInsert(player);
        } catch (Exception e) {
            return new CompoundTag();
        }

        return contents;
    }

    private static void insertGravestoneContents(ServerLevel level, Player player, BlockPos gravestonePos, CompoundTag contents) {
        if (!(level.getBlockEntity(gravestonePos) instanceof TechnicalGravestoneBlockEntity gravestone)) return;

        gravestone.setContents(contents);
        gravestone.setGraveOwner(new GraveOwner(player.getGameProfile()));
        gravestone.setSpawnDate(GravestoneTime.READABLE.format(new Date()), level.getGameTime());

        level.sendBlockUpdated(gravestonePos, gravestone.getBlockState(), gravestone.getBlockState(), Block.UPDATE_CLIENTS);
    }

    private static GlobalPos getPlacementPos(ServerLevel level, Player player, GlobalPos deathPos) {
        DimensionType dimension = level.dimensionType();
        GlobalPos clampedDeathPos = VersionUtil.createGlobalPos(deathPos.dimension(), deathPos.pos().atY(
                Mth.clamp(deathPos.pos().getY(), dimension.minY(), dimension.minY() + dimension.height())
        ));
        GlobalPos validPos = GravestonePlacement.getRedirectableValidPos(level, player, clampedDeathPos);

        if (validPos == null || level.getServer().getLevel(validPos.dimension()) == null) return null;

        return validPos;
    }

    protected static void placeGravestone(ServerLevel level, BlockPos pos) {
        BlockState gravestoneBlock = GravestonesRegistry.GRAVESTONE_TECHNICAL.defaultBlockState();
        if (level.getBlockState(pos).getFluidState().is(FluidTags.WATER)) {
            gravestoneBlock = gravestoneBlock.setValue(BlockStateProperties.WATERLOGGED, true);
        }

        level.destroyBlock(pos, true);
        level.setBlockAndUpdate(pos, gravestoneBlock);
    }
}
