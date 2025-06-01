package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GravestonesApi {
    private static final Map<Identifier, GravestoneDataType> DATA_TYPES = new HashMap<>();
    private static final List<ModSupport> modSupports = new ArrayList<>();

    public static List<ModSupport> getModSupports() {
        return modSupports;
    }

    /**
     * Registers a type of data that gravestones save, and how gravestones should handle that data.
     */
    public static void registerDataType(Identifier identifier, GravestoneDataType dataType) {
        DATA_TYPES.put(identifier, dataType);
    }

    /**
     * Registers a list of instructions (ModSupport instance) so that data from other mods can also be saved into gravestones if necessary.
     *
     * @param support ModSupport instance
     */
    public static void registerModSupport(ModSupport support) {
        modSupports.add(support);
    }

    public static NbtCompound getDataToInsert(PlayerEntity player) {
        NbtCompound contents = new NbtCompound();

        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            contents.put(
                    entry.getKey().toString(),
                    entry.getValue().getDataToInsert(player)
            );
        }

        return contents;
    }

    public static void onBreak(TechnicalGravestoneBlockEntity entity) {
        onBreak(entity.getWorld(), entity.getPos(), entity.getDecay(), entity.getContents());
    }

    public static void onBreak(World world, BlockPos pos, int decay, NbtCompound contents) {
        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            entry.getValue().onBreak(
                    world,
                    pos,
                    decay,
                    contents.get(entry.getKey().toString())
            );
        }
    }

    public static void onCollect(PlayerEntity player, int decay, NbtCompound contents) {
        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            entry.getValue().onCollect(
                    player,
                    decay,
                    contents.get(entry.getKey().toString())
            );
        }
    }

    /**
     * Checks against all {@link ModSupport}s to see whether an item stack should be inserted into the gravestone or not.
     *
     * @param player The player who has died.
     * @param stack The stack being checked.
     * @return Whether the item should be inserted.
     */
    public static boolean shouldSkipItem(PlayerEntity player, ItemStack stack) {
        for (ModSupport support : GravestonesApi.getModSupports()) {
            if (!support.shouldPutItemInGravestone(player, stack)) {
                return true;
            }
        }
        return false;
    }
}
