package net.pneumono.gravestones.gravestones;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.DeprecatedEventHandler;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.event.GravestoneCollectionEvents;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.multiversion.GraveOwner;
import net.pneumono.gravestones.multiversion.VersionUtil;
import net.pneumono.pneumonocore.util.MultiVersionUtil;

public class GravestoneCollection extends GravestoneManager {
    public static boolean collect(ServerLevel level, Player player, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity gravestone) {

            info("----- Beginning Gravestone Collection -----");
            info("If you don't want to see this, disable 'Console Info' in the configs!");
            boolean success = collect(level, player, pos, gravestone);
            info("----- Finishing Gravestone Collection -----");

            return success;
        } else {
            return false;
        }
    }

    private static boolean collect(ServerLevel level, Player player, BlockPos pos, TechnicalGravestoneBlockEntity gravestone) {
        // Check the player is allowed to open the gravestone
        info("Performing checks...");
        if (player instanceof FakePlayer) {
            info("Player cannot collect gravestone because they are a FakePlayer");
            return false;
        }

        if (player.isDeadOrDying()) {
            info("Player cannot collect gravestone because they are dead");
            return false;
        }

        GraveOwner graveOwner = gravestone.getGraveOwner();
        if (graveOwner == null) {
            info("Player cannot collect gravestone because it has no owner");
            message(player, Component.translatable("gravestones.cannot_open_no_owner"));
            return false;
        }

        boolean isOwner = graveOwner.getUuid().equals(VersionUtil.getId(player.getGameProfile()));
        if (!isOwner && GravestonesConfig.GRAVESTONE_ACCESSIBLE_OWNER_ONLY.getValue()) {
            info("Player cannot collect gravestone because they are not the owner");
            message(player, Component.translatable("gravestones.cannot_open_wrong_player", graveOwner.getNotNullName()));
            return false;
        }

        MinecraftServer server = level.getServer();
        GlobalPos globalPos = MultiVersionUtil.createGlobalPos(level.dimension(), pos);
        Component component = GravestoneCollectionEvents.runCancelCollect(server, player, globalPos, gravestone);
        if (component != null) {
            message(player, component);
        }

        info("All checks passed");

        // Run BeforeCollect listeners
        GravestoneCollectionEvents.runBeforeCollect(server, player, globalPos, gravestone);

        // Return gravestone contents
        info("Returning gravestone contents...");
        boolean success = GravestonesApi.onCollect(level, pos, player, gravestone.getTotalDamage(), gravestone.getContents());
        if (!success) {
            error("Some gravestone contents had errors, so gravestone collection failed.");
            return false;
        }
        gravestone.setContents(new CompoundTag());

        // Log grave collection
        String uuid = "";
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            uuid = " (" + VersionUtil.getId(player.getGameProfile()) + ")";
        }
        if (isOwner) {
            Gravestones.LOGGER.info("{}{} has found their grave at {}", player.getName().getString(), uuid, GravestoneManager.posToString(pos));
        } else {
            Gravestones.LOGGER.info("{}{} has found {}{}'s grave at {}",
                    player.getName().getString(), uuid,
                    graveOwner.getNotNullName(), graveOwner.getUuid(),
                    pos.toString()
            );
        }

        // Broadcast chat message
        info("Broadcasting chat message...");
        if (GravestonesConfig.BROADCAST_COLLECT_IN_CHAT.getValue()) {
            MutableComponent text;
            if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
                if (isOwner) {
                    text = Component.translatable("gravestones.player_collected_grave_at_coords", player.getName().getString(), GravestoneManager.posToString(pos));
                } else {
                    text = Component.translatable("gravestones.player_collected_others_grave_at_coords", player.getName().getString(), graveOwner.getNotNullName(), GravestoneManager.posToString(pos));
                }
            } else {
                if (isOwner) {
                    text = Component.translatable("gravestones.player_collected_grave", player.getName().getString());
                } else {
                    text = Component.translatable("gravestones.player_collected_others_grave", player.getName().getString(), graveOwner.getNotNullName());
                }
            }
            server.getPlayerList().broadcastSystemMessage(text, false);
        }

        // Break block
        info("Breaking gravestone...");
        level.destroyBlock(pos, true);

        DeprecatedEventHandler.gravestoneCollectedCallback(level, player, pos);

        // Run AfterCollect listeners
        GravestoneCollectionEvents.runAfterCollect(server, player, globalPos);

        return true;
    }

    private static void message(Player player, Component component) {
        //? if >=26.1 {
        player.sendOverlayMessage(component);
        //?} else {
        /*player.displayClientMessage(component, true);
        *///?}
    }
}
