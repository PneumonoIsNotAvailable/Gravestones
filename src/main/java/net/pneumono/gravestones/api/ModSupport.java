package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;

public abstract class ModSupport {
    /**
     * Called when a new grave is created.<p>
     * Use {@link TechnicalGravestoneBlockEntity#addOrReplaceModData(String, NbtCompound)} to add new data.
     * It is recommended to also check any items with {@link GravestonesApi#shouldSkipItem(PlayerEntity, ItemStack)}, to maintain compatibility.
     *
     * @param player The player who has died.
     * @param entity The gravestone block entity that data can be inserted into.
     */
    public void insertData(PlayerEntity player, TechnicalGravestoneBlockEntity entity) {

    }

    /**
     * Called when a gravestone is broken. This happens when gravestones are collected, but also when a Creative Mode player breaks a gravestone, and if other mods create new ways of breaking gravestones.<p>
     * Exists so that if graves are broken through other means, items can still be dropped on the ground.<p>
     * {@link TechnicalGravestoneBlockEntity#getModData(String)} can be used to get previously added data.<p>
     * Remember to null-check mod data, since it's possible that the mod being supported was added in after a gravestone had already been created.
     *
     * @param entity The gravestone block entity that has been broken.
     */
    public void onBreak(TechnicalGravestoneBlockEntity entity) {

    }

    /**
     * Called when a player collects a gravestone. If owner-only access is disabled in the configs, this may be a player other than the grave's owner.<p>
     * Keep in mind the fact that {@link ModSupport#onBreak(TechnicalGravestoneBlockEntity)} will be called after this, as well anything other mods have added to the dropInventory method, so you may need to remove data to prevent it being duplicated<p>
     * {@link TechnicalGravestoneBlockEntity#getModData(String)} can be used to get previously added data.<p>
     * Remember to null-check mod data, since it's possible that the mod being supported was added in after a gravestone had already been created.
     *
     * @param player The player collecting the gravestone.
     * @param entity The gravestone block entity that is being collected.
     */
    public void onCollect(PlayerEntity player, TechnicalGravestoneBlockEntity entity) {

    }

    /**
     * Called for each item in the player's inventory to see if the item should be put in the gravestone.<p>
     * If it returns {@code true}, the item is put in the gravestone as normal (assuming it passes checks from all other {@link ModSupport}s).<p>
     * If it returns {@code false}, the item is "skipped" by the gravestone, and is not put in the gravestone or cleared from the player's inventory. If nothing else is done other than returning {@code false}, this will result in the item being dropped on the ground, like in vanilla.<p>
     * This is ideal for supporting mods that would do other things on death, since it should just skip the gravestone process, then the other mod's code should run. However, if you return {@code false} and do not want this behaviour, make sure to set the stack's count to 0 using {@link ItemStack#setCount(int)} to make the item "disappear".<p>
     * This can also be used for doing other things with items on death (for example, an item that decrements by 1 on death) by simply changing whatever is needed about the item stack, and then returning {@code true}.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @return Whether the item should be put in the gravestone.
     */
    public boolean shouldPutItemInGravestone(PlayerEntity player, ItemStack stack) {
        return true;
    }
}
