package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;

public abstract class ModSupport {
    /**
     * Called when a new grave is created.<p>
     * Use {@link TechnicalGravestoneBlockEntity#addOrReplaceModData(String, NbtCompound)} to add new data.
     *
     * @param player The player who has died.
     * @param entity The gravestone block entity that data can be inserted into.
     */
    public void insertGravestoneData(PlayerEntity player, TechnicalGravestoneBlockEntity entity) {

    }

    /**
     * Called when a gravestone is broken. This happens when gravestones are collected, but also when a Creative Mode player breaks a gravestone, and if other mods create new ways of breaking gravestones.<p>
     * Exists so that if graves are broken through other means, items can still be dropped on the ground.<p>
     * {@link TechnicalGravestoneBlockEntity#getModData(String)} can be used to get previously added data.
     *
     * @param entity The gravestone block entity that has been broken.
     */
    public void onGravestoneBreak(TechnicalGravestoneBlockEntity entity) {

    }

    /**
     * Called when a player collects a gravestone. If owner-only access is disabled in the configs, this may be a player other than the grave's owner.<p>
     * Keep in mind the fact that {@link ModSupport#onGravestoneBreak(TechnicalGravestoneBlockEntity)} will be called after this, as well anything other mods have added to the dropInventory method, so you may need to remove data to prevent it being duplicated<p>
     * {@link TechnicalGravestoneBlockEntity#getModData(String)} can be used to get previously added data.
     *
     * @param player The player collecting the gravestone.
     * @param entity The gravestone block entity that is being collected.
     */
    public void onGravestoneCollect(PlayerEntity player, TechnicalGravestoneBlockEntity entity) {

    }
}
