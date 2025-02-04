package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GravestonesApi {
    private static final List<ModSupport> modSupports = new ArrayList<>();

    public static List<ModSupport> getModSupports() {
        return modSupports;
    }

    /**
     * Registers a list of instructions (ModSupport instance) so that data from other mods can also be saved into gravestones if necessary.
     *
     * @param support ModSupport instance
     */
    @SuppressWarnings("unused")
    public static void registerModSupport(ModSupport support) {
        modSupports.add(support);
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
