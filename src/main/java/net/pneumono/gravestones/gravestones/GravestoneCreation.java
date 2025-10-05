package net.pneumono.gravestones.gravestones;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.pneumono.gravestones.api.GravestonePlacedCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.multiversion.GraveOwner;
import net.pneumono.gravestones.multiversion.VersionUtil;
import net.pneumono.pneumonocore.util.MultiVersionUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GravestoneCreation extends GravestoneManager {
    public static void create(PlayerEntity player) {
        World world = MultiVersionUtil.getWorld(player);
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        checkConsoleInfoConfig();

        info("----- Beginning Gravestone Creation -----");
        info("If you don't want to see this, disable 'Console Info' in the configs!");
        create(serverWorld, player);
        info("----- Finishing Gravestone Creation -----");
    }

    private static void create(ServerWorld deathWorld, PlayerEntity player) {
        GlobalPos deathPos = new GlobalPos(deathWorld.getRegistryKey(), player.getBlockPos());
        MinecraftServer server = deathWorld.getServer();

        // Check if placement should be cancelled
        info("Checking if Gravestone Placement should be cancelled...");
        if (CancelGravestonePlacementCallback.EVENT.invoker().shouldCancel(
                deathWorld, player, deathPos
        )) {
            info("Placement cancelled!");
            return;
        }
        info("Placement not cancelled");

        // Read gravestone history
        CompletableFuture<List<RecentGraveHistory>> historiesFuture = CompletableFuture.supplyAsync(() -> {
            info("Reading gravestone history...");
            return GravestoneDataSaving.readHistories(server);
        });

        // Create contents data
        info("Creating gravestone contents data...");
        NbtCompound contents = createContentsData(player);
        // Backup contents data
        CompletableFuture.runAsync(() -> {
            info("Backing up gravestone contents data...");
            GravestoneDataSaving.saveBackup(contents, player);
        });

        // Calculate placement position
        info("Calculating gravestone placement position...");
        GlobalPos gravestonePos = getPlacementPos(deathWorld, player, deathPos);

        historiesFuture.thenAcceptAsync(histories -> {
            histories = new ArrayList<>(histories);
            RecentGraveHistory history = getHistory(histories, player.getUuid());

            // Create new gravestone history
            info("Updating gravestone history...");
            RecentGraveHistory newHistory;
            if (history == null) {
                newHistory = new RecentGraveHistory(player.getUuid(), gravestonePos);
            } else {
                newHistory = history.getShifted(gravestonePos);
            }
            histories.add(newHistory);

            // Write new gravestone history
            info("Writing updated gravestone history...");
            GravestoneDataSaving.writeData(server, histories);
        });

        String playerName = VersionUtil.getName(player.getGameProfile());

        // Place gravestone
        info("Placing gravestone...");
        if (gravestonePos != null && server.getWorld(gravestonePos.dimension()) instanceof ServerWorld graveWorld) {
            placeGravestone(server, gravestonePos);
            Gravestones.LOGGER.info("Placed {}'s Gravestone at {}", playerName, posToString(gravestonePos));
        } else {
            GravestonesApi.onBreak(deathWorld, deathPos.pos(), 0, contents == null ? new NbtCompound() : contents);
            Gravestones.LOGGER.info("Failed to place {}'s Gravestone! The items have been dropped on the ground", playerName);
            return;
        }

        // Insert gravestone contents
        info("Inserting contents into gravestone...");
        insertGravestoneContents(graveWorld, player, gravestonePos, contents);

        // Broadcast chat message
        info("Broadcasting chat message... (if enabled)");
        if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
            server.getPlayerManager().broadcast(Text.translatable("gravestones.grave_spawned", playerName, posToString(gravestonePos.pos())), false);
        }

        info("Damaging existing gravestones... (if enabled)");
        try {
            // Damage existing gravestones
            RecentGraveHistory history = getHistory(new ArrayList<>(historiesFuture.get()), player.getUuid());
            if (history != null) {
                GravestoneDecay.deathDamageOldGravestones(server, history.getList(), gravestonePos);
            }
        } catch (ExecutionException | InterruptedException e) {
            error("Failed to damage existing gravestones", e);
        }

        // Callbacks
        info("Invoking GravestonePlacedCallbacks...");
        GravestonePlacedCallback.EVENT.invoker().afterGravestonePlace(deathWorld, player, deathPos, gravestonePos);
    }

    public static RecentGraveHistory getHistory(List<RecentGraveHistory> histories, UUID uuid) {
        RecentGraveHistory history = null;
        for (int i = 0; i < histories.size(); ++i) {
            RecentGraveHistory checkedHistory = histories.get(i);
            if (checkedHistory.owner().equals(uuid)) {
                history = histories.remove(i);
                break;
            }
        }

        return history;
    }

    private static NbtCompound createContentsData(PlayerEntity player) {
        NbtCompound contents;
        try {
            contents = GravestonesApi.getDataToInsert(player);
        } catch (Exception e) {
            return new NbtCompound();
        }

        return contents;
    }

    private static void insertGravestoneContents(ServerWorld world, PlayerEntity player, GlobalPos gravestonePos, NbtCompound contents) {
        if (!(world.getBlockEntity(gravestonePos.pos()) instanceof TechnicalGravestoneBlockEntity gravestone)) return;

        gravestone.setContents(contents);
        gravestone.setGraveOwner(new GraveOwner(player.getGameProfile()));
        gravestone.setSpawnDate(GravestoneTime.READABLE.format(new Date()), world.getTime());

        world.updateListeners(gravestonePos.pos(), gravestone.getCachedState(), gravestone.getCachedState(), Block.NOTIFY_LISTENERS);
    }

    private static GlobalPos getPlacementPos(ServerWorld world, PlayerEntity player, GlobalPos deathPos) {
        DimensionType dimension = world.getDimension();
        GlobalPos clampedDeathPos = new GlobalPos(deathPos.dimension(), deathPos.pos().withY(
                MathHelper.clamp(deathPos.pos().getY(), dimension.minY(), dimension.minY() + dimension.height())
        ));
        GlobalPos validPos = GravestonePlacement.getRedirectableValidPos(world, player, clampedDeathPos);

        if (validPos == null || world.getServer().getWorld(validPos.dimension()) == null) return null;

        return validPos;
    }

    protected static void placeGravestone(MinecraftServer server, GlobalPos gravestonePos) {
        ServerWorld world = server.getWorld(gravestonePos.dimension());

        if (world == null) return;

        BlockPos pos = gravestonePos.pos();
        BlockState gravestoneBlock = GravestonesRegistry.GRAVESTONE_TECHNICAL.getDefaultState();
        if (world.getBlockState(pos).getFluidState().isIn(FluidTags.WATER)) {
            gravestoneBlock = gravestoneBlock.with(Properties.WATERLOGGED, true);
        }

        world.breakBlock(pos, true);
        world.setBlockState(pos, gravestoneBlock);
    }
}
