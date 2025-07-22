package net.pneumono.gravestones.gravestones;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
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

import java.util.*;

public class GravestoneCreation extends GravestoneManager {
    public static void handleGravestones(PlayerEntity player) {
        World world = player.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        handle(serverWorld, player);
    }

    public static void handle(ServerWorld deathWorld, PlayerEntity player) {
        GlobalPos deathPos = new GlobalPos(deathWorld.getRegistryKey(), player.getBlockPos());

        // Callbacks
        if (CancelGravestonePlacementCallback.EVENT.invoker().shouldCancel(
                deathWorld, player, deathPos
        )) {
            return;
        }

        // Data Creation & Saving
        NbtCompound contents = createAndSaveData(player);

        // Placement
        GlobalPos gravestonePos = placeGravestone(deathWorld, player, deathPos);

        if (gravestonePos != null && deathWorld.getServer().getWorld(gravestonePos.dimension()) instanceof ServerWorld graveWorld) {
            Gravestones.LOGGER.info("Placed {}'s Gravestone at {}", player.getGameProfile().getName(), posToString(gravestonePos.pos()));
        } else {
            Gravestones.LOGGER.info("Failed to place {}'s Gravestone!", player.getGameProfile().getName());
            return;
        }

        // Insertion
        insertGravestoneData(graveWorld, player, gravestonePos, contents);

        // Update Existing
        updateExistingGravestones(graveWorld, player.getGameProfile(), gravestonePos);

        // Broadcast
        if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
            deathWorld.getServer().getPlayerManager().broadcast(Text.translatable("gravestones.grave_spawned", player.getGameProfile().getName(), posToString(gravestonePos.pos())), false);
        }

        // Callbacks
        GravestonePlacedCallback.EVENT.invoker().afterGravestonePlace(deathWorld, player, deathPos, gravestonePos);
    }

    private static NbtCompound createAndSaveData(PlayerEntity player) {
        Date date = new Date();
        NbtCompound contents;
        try {
            contents = GravestonesApi.getDataToInsert(player);
        } catch (Exception e) {
            return null;
        }

        GravestoneDataSaving.saveDeathData(contents, player, date);

        return contents;
    }

    private static void insertGravestoneData(ServerWorld world, PlayerEntity player, GlobalPos gravestonePos, NbtCompound contents) {
        if (!(world.getBlockEntity(gravestonePos.pos()) instanceof TechnicalGravestoneBlockEntity gravestone)) return;

        gravestone.setContents(contents);
        gravestone.setGraveOwner(new ProfileComponent(player.getGameProfile()));
        gravestone.setSpawnDate(GravestoneTime.READABLE.format(new Date()), world.getTime());

        world.updateListeners(gravestonePos.pos(), gravestone.getCachedState(), gravestone.getCachedState(), Block.NOTIFY_LISTENERS);
    }

    protected static GlobalPos placeGravestone(ServerWorld deathWorld, PlayerEntity player, GlobalPos deathPos) {
        DimensionType dimension = deathWorld.getDimension();
        GlobalPos clampedDeathPos = new GlobalPos(deathPos.dimension(), deathPos.pos().withY(
                MathHelper.clamp(deathPos.pos().getY(), dimension.minY(), dimension.minY() + dimension.height())
        ));
        GlobalPos validPos = GravestonePlacement.getRedirectableValidPos(deathWorld, player, clampedDeathPos);

        if (validPos == null) return null;
        World validWorld = deathWorld.getServer().getWorld(validPos.dimension());
        if (validWorld == null) return null;

        BlockState gravestoneBlock = GravestonesRegistry.GRAVESTONE_TECHNICAL.getDefaultState();
        if (validWorld.getBlockState(validPos.pos()).getFluidState().isIn(FluidTags.WATER)) {
            gravestoneBlock = gravestoneBlock.with(Properties.WATERLOGGED, true);
        }

        validWorld.breakBlock(validPos.pos(), true);
        validWorld.setBlockState(validPos.pos(), gravestoneBlock);

        return validPos;
    }

    private static void updateExistingGravestones(ServerWorld world, GameProfile playerProfile, GlobalPos gravestonePos) {
        List<GlobalPos> oldGravePositions = GravestoneDataSaving.readAndWriteData(world.getServer(), playerProfile.getId(), gravestonePos);
        GravestoneDecay.deathDamageOldGravestones(world, oldGravePositions, gravestonePos);
    }
}
