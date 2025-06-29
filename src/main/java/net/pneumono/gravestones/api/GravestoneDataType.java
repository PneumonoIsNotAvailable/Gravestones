package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class GravestoneDataType {
    /**
     * Called when a new grave is created.<p>
     * Normal inventory dropping on death still occurs after this is called, so make sure to clear/remove data when it's inserted to prevent it being duplicated.<p>
     * You can avoid doing this if you want normal death functionality under certain conditions (e.g. a specific item that should drop normally rather than be put into the gravestone should simply be ignored by this method and not cleared)<p>
     * It is recommended to also check any items with {@link GravestonesApi#shouldSkipItem(PlayerEntity, ItemStack)}, to maintain compatibility.
     *
     * @param player The player who has died.
     */
    public abstract void writeData(WriteView view, PlayerEntity player);

    /**
     * Called when a gravestone is broken. This happens when a Creative Mode player breaks a gravestone, and also if other mods create new ways of breaking gravestones.<p>
     * Exists so that data is not lost if graves are broken through other means.<p>
     * Remember to add null checks, since it's possible that the mod being supported was added in after a gravestone had already been created.
     */
    public abstract void onBreak(ReadView view, World world, BlockPos pos, int decay);

    /**
     * Called when a player collects a gravestone. This may be a player other than the grave's owner, if owner-only access is disabled in the configs or if commands are used.<p>
     * Remember to add null checks, since it's possible that the mod being supported was added in after a gravestone had already been created.
     *
     * @param player The player collecting the gravestone.
     * @param decay The decay stage of the gravestone being collected.
     */
    public abstract void onCollect(ReadView view, World world, BlockPos pos, PlayerEntity player, int decay);
}
