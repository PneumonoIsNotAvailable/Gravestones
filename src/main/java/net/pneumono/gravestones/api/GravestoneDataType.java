package net.pneumono.gravestones.api;

import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.content.ExperienceDataType;
import net.pneumono.gravestones.content.PlayerInventoryDataType;
import net.pneumono.gravestones.gravestones.GravestoneManager;

import java.util.Collection;

/**
 * Represents a type of data that a gravestone can store.
 *
 * <p>Data types do not store data themselves, instead they handle data passed to them.
 * Everything here should only ever be called on the logical server.
 *
 * <p>They are registered using {@link GravestonesApi#registerDataType}.
 *
 * <p>See {@link PlayerInventoryDataType}
 * and {@link ExperienceDataType} for examples.
 */
public abstract class GravestoneDataType extends GravestoneManager {
    /**
     * Called when a new grave is created.
     *
     * <p>Normal inventory dropping on death still occurs after this is called,
     * so make sure to clear/remove data when it's inserted to prevent it being duplicated.
     *
     * <p>When handling items, make sure to call {@link GravestonesApi#onInsertItem},
     * and check the item with {@link GravestonesApi#shouldSkipItem}.
     * If an item stack should be skipped, do not insert or remove it,
     * since it should fall back to non-gravestones processing.
     *
     * @param player The player who has died.
     */
    public abstract void writeData(NbtCompound nbt, DynamicOps<NbtElement> ops, PlayerEntity player) throws Exception;

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
     * <p>Do not assume that the {@code NbtCompound} contains all the data you wrote,
     * since it's possible that the gravestone was created before this data type was added.
     *
     * @param pos The position of the gravestone.
     * @param decay The decay stage of the gravestone being collected.
     */
    public abstract void onBreak(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, int decay) throws Exception;

    /**
     * Called when a player collects a gravestone.
     *
     * <p>This may be a player other than the grave's owner,
     * if owner-only access is disabled in the configs or if commands are used.
     *
     * <p>When handling XP, make sure to use {@link GravestonesApi#getDecayedExperience}.
     *
     * <p>Do not assume that the {@code NbtCompound} contains all the data you wrote,
     * since it's possible that the gravestone was created before this data type was added.
     *
     * @param pos The position of the gravestone.
     * @param player The player collecting the gravestone.
     * @param decay The decay stage of the gravestone being collected.
     */
    public abstract void onCollect(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, PlayerEntity player, int decay) throws Exception;

    public void dropStack(World world, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty()) {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    public void dropStacks(World world, BlockPos pos, Collection<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            dropStack(world, pos, stack);
        }
    }

    public void dropStack(PlayerEntity player, ItemStack stack) {
        if (!player.giveItemStack(stack)) {
            ItemEntity itemEntity = player.dropItem(stack, false);
            if (itemEntity != null) {
                itemEntity.resetPickupDelay();
                itemEntity.setOwner(player.getUuid());
            }
        }
    }

    public void dropStacks(PlayerEntity player, Collection<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            dropStack(player, stack);
        }
    }
}
