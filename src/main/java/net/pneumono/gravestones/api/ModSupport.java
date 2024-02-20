package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.pneumono.gravestones.content.entity.GravestoneBlockEntity;

public interface ModSupport {
    /**
     * Called when a new grave is created.<p>
     * Use {@link GravestoneBlockEntity#addOrReplaceModData(String, NbtCompound)} to add new data.
     *
     * @param player The player who has died.
     * @param entity The gravestone block entity that data can be inserted into.
     */
    void insertData(PlayerEntity player, GravestoneBlockEntity entity);

    /**
     * Called when a gravestone is broken. This happens when gravestones are collected, but also when a Creative Mode player breaks a gravestone, and if other mods create new ways of breaking gravestones.<p>
     * Exists so that if graves are broken through other means, items can still be dropped on the ground.<p>
     * {@link GravestoneBlockEntity#getModData(String)} can be used to get previously added data.
     *
     * @param entity The gravestone block entity that has been broken.
     */
    void onBreak(GravestoneBlockEntity entity);

    /**
     * Called when a player collects a gravestone. If owner-only access is disabled in the configs, this may be a player other than the grave's owner.<p>
     * Keep in mind the fact that {@link ModSupport#onBreak(GravestoneBlockEntity)} will be called after this, as well anything other mods have added to the dropInventory method, so you may need to remove data to prevent it being duplicated<p>
     * {@link GravestoneBlockEntity#getModData(String)} can be used to get previously added data.
     *
     * @param player The player collecting the gravestone.
     * @param entity The gravestone block entity that is being collected.
     */
    void onCollect(PlayerEntity player, GravestoneBlockEntity entity);
}
