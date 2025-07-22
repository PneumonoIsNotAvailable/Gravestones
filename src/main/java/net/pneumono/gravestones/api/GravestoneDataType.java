package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.content.ExperienceDataType;
import net.pneumono.gravestones.content.PlayerInventoryDataType;

/**
 * Represents a type of data that a gravestone can store.
 *
 * <p>Data types do not store data themselves, instead they handle data passed to them.
 *
 * <p>They are registered using {@link GravestonesApi#registerDataType}.
 *
 * <p>See {@link PlayerInventoryDataType}
 * and {@link ExperienceDataType} for examples.
 */
public abstract class GravestoneDataType {
    /**
     * Called when a new grave is created.
     *
     * <p>Normal inventory dropping on death still occurs after this is called,
     * so make sure to clear/remove data when it's inserted to prevent it being duplicated.
     *
     * <p>It is recommended to check any item stacks with {@link GravestonesApi#shouldSkipItem},
     * to maintain compatibility. If an item stack should be skipped, do not insert or remove it,
     * since it should fall back to vanilla processing.
     *
     * @param player The player who has died.
     */
    public abstract void writeData(WriteView view, PlayerEntity player);

    /**
     * Called when a gravestone is broken, unless it was broken by a player collecting it.
     *
     * <p>This can happen when a Creative Mode player breaks a gravestone manually,
     * or if other mods create new ways of breaking gravestones.
     *
     * <p>Exists so that data is not lost if graves are broken through other means.
     * In most cases, this involves dropping things on the ground (e.g. items or XP orbs).
     *
     * <p>When handling XP, make sure to use {@link GravestonesApi#getDecayedExperience}.
     *
     * <p>Remember to null check the {@code ReadView},
     * since it's possible that the gravestone was created before this data type was added.
     *
     * @param pos The position of the gravestone.
     * @param decay The decay stage of the gravestone being collected.
     */
    public abstract void onBreak(ReadView view, World world, BlockPos pos, int decay);

    /**
     * Called when a player collects a gravestone.
     *
     * <p>This may be a player other than the grave's owner,
     * if owner-only access is disabled in the configs or if commands are used.
     *
     * <p>When handling XP, make sure to use {@link GravestonesApi#getDecayedExperience}.
     *
     * <p>Remember to null check the {@code ReadView},
     * since it's possible that the gravestone was created before this data type was added.
     *
     * @param pos The position of the gravestone.
     * @param player The player collecting the gravestone.
     * @param decay The decay stage of the gravestone being collected.
     */
    public abstract void onCollect(ReadView view, World world, BlockPos pos, PlayerEntity player, int decay);
}
