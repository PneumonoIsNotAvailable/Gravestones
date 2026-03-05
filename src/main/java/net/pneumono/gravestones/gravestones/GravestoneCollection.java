package net.pneumono.gravestones.gravestones;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestoneCollectedCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.multiversion.GraveOwner;
import net.pneumono.gravestones.multiversion.VersionUtil;

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
            player.displayClientMessage(Component.translatable("gravestones.cannot_open_no_owner"), true);
            return false;
        }

        boolean isOwner = graveOwner.getUuid().equals(VersionUtil.getId(player.getGameProfile()));
        if (!isOwner && GravestonesConfig.GRAVESTONE_ACCESSIBLE_OWNER_ONLY.getValue()) {
            info("Player cannot collect gravestone because they are not the owner");
            player.displayClientMessage(Component.translatable("gravestones.cannot_open_wrong_player", graveOwner.getNotNullName()), true);
            return false;
        }
        info("All checks passed");

        // Return gravestone contents
        info("Returning gravestone contents...");
        GravestonesApi.onCollect(level, pos, player, gravestone.getDecay(), gravestone.getContents());
        CompoundTag contents = gravestone.getContents();
        if (!contents.isEmpty()) {
            warn("Some gravestone contents were not returned: {}", contents);
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
            level.getServer().getPlayerList().broadcastSystemMessage(text, false);
        }

        // Break block
        info("Breaking gravestone...");
        level.destroyBlock(pos, true);

        GravestoneCollectedCallback.EVENT.invoker().afterGravestoneCollect(level, player, pos);

        return true;
    }
}
