package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.pneumono.gravestones.content.entity.GravestoneBlockEntity;

public interface ModSupport {
    /**
     * Called when a new grave is created. Use {@link GravestoneBlockEntity#addModData(String, NbtCompound)} to add new data.
     *
     * @param player The player who has died.
     * @param entity The gravestone block entity that data can be inserted into.
     */
    void insertData(PlayerEntity player, GravestoneBlockEntity entity);

    /**
     * Called when a gravestone is broken instead of being collected. This should only happen when a Creative Mode player breaks a gravestone, but other mods may introduce new ways breaking gravestones. {@link GravestoneBlockEntity#getModData(String)} can be used to get previously added data.
     *
     * @param entity The gravestone block entity that has been broken.
     */
    void onBreak(GravestoneBlockEntity entity);

    /**
     * Called when a player collects a gravestone. If owner-only access is disabled in the configs, this may be a player other than the grave's owner. {@link GravestoneBlockEntity#getModData(String)} can be used to get previously added data.
     *
     * @param player The player collecting the gravestone.
     * @param entity The gravestone block entity that is being collected.
     */
    void onCollect(PlayerEntity player, GravestoneBlockEntity entity);
}
