package net.pneumono.gravestones.gravestones;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;

public class GravestoneCollection extends GravestoneManager {
    public static boolean collect(ServerWorld world, PlayerEntity player, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity gravestone) {

            info("----- Beginning Gravestone Collection -----");
            info("If you don't want to see this, disable 'Console Info' in the configs!");
            boolean success = collect(world, player, pos, gravestone);
            info("----- Finishing Gravestone Collection -----");

            return success;
        } else {
            return false;
        }
    }

    private static boolean collect(ServerWorld world, PlayerEntity player, BlockPos pos, TechnicalGravestoneBlockEntity gravestone) {
        // Check the player is allowed to open the gravestone
        info("Performing checks...");
        if (player instanceof FakePlayer) {
            info("Player cannot collect gravestone because they are a FakePlayer");
            return false;
        }

        if (player.isDead()) {
            info("Player cannot collect gravestone because they are dead");
            return false;
        }

        ProfileComponent graveOwner = gravestone.getGraveOwner();
        if (graveOwner == null) {
            info("Player cannot collect gravestone because it has no owner");
            player.sendMessage(Text.translatable("gravestones.cannot_open_no_owner"), true);
            return true;
        }

        boolean isOwner = graveOwner.gameProfile().getId().equals(player.getGameProfile().getId());
        if (!isOwner && GravestonesConfig.GRAVESTONE_ACCESSIBLE_OWNER_ONLY.getValue()) {
            info("Player cannot collect gravestone because they are not the owner");
            player.sendMessage(Text.translatable("gravestones.cannot_open_wrong_player", graveOwner.name().orElse("???")), true);
            return true;
        }
        info("All checks passed");

        // Return gravestone contents
        info("Returning gravestone contents...");
        GravestonesApi.onCollect(world, pos, player, gravestone.getDecay(), gravestone.getContents());
        NbtCompound contents = gravestone.getContents();
        if (!contents.isEmpty()) {
            warn("Some gravestone contents were not returned: {}", contents);
        }
        gravestone.setContents(new NbtCompound());

        // Log grave collection
        String uuid = "";
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            uuid = " (" + player.getGameProfile().getId() + ")";
        }
        if (isOwner) {
            Gravestones.LOGGER.info("{}{} has found their grave at {}", player.getName().getString(), uuid, GravestoneManager.posToString(pos));
        } else {
            Gravestones.LOGGER.info("{}{} has found {}{}'s grave at {}",
                    player.getName().getString(), uuid,
                    graveOwner.name().orElse("???"), graveOwner.uuid().orElse(null),
                    pos.toString()
            );
        }

        // Broadcast chat message
        info("Broadcasting chat message...");
        if (GravestonesConfig.BROADCAST_COLLECT_IN_CHAT.getValue()) {
            MutableText text;
            if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
                if (isOwner) {
                    text = Text.translatable("gravestones.player_collected_grave_at_coords", player.getName().getString(), GravestoneManager.posToString(pos));
                } else {
                    text = Text.translatable("gravestones.player_collected_others_grave_at_coords", player.getName().getString(), graveOwner.name().orElse("???"), GravestoneManager.posToString(pos));
                }
            } else {
                if (isOwner) {
                    text = Text.translatable("gravestones.player_collected_grave", player.getName().getString());
                } else {
                    text = Text.translatable("gravestones.player_collected_others_grave", player.getName().getString(), graveOwner.name().orElse("???"));
                }
            }
            world.getServer().getPlayerManager().broadcast(text, false);
        }

        // Break block
        info("Breaking gravestone...");
        world.breakBlock(pos, true);

        return true;
    }
}
