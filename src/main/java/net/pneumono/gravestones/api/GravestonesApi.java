package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Contains most methods needed for adding Gravestones support for other mods.
 *
 * <p>Gravestones provides several tags that should be used instead of events where possible:
 * <ul>
 *     <li>Items or enchantments in the {@code gravestones:skips_gravestones} tags will be skipped by gravestones.
 *     <li>Blocks in the {@code gravestones:gravestone_irreplaceable} will never be replaced by gravestones.
 * </ul>
 *
 * <p>Most mods will not need to use anything other than the tags,
 * {@link InsertGravestoneItemCallback} (for items with different behavior on death)
 * and {@link #registerDataType} (for custom data that also needs to be saved on death).
 *
 * <p>There is a page on the <a href="https://github.com/PneumonoIsNotAvailable/Gravestones/wiki/Compatibility">Gravestones Wiki</a>
 * for Gravestones' API features, however the documentation here is more detailed and up-to-date.
 *
 * @see GravestoneDataType
 * @see CancelGravestonePlacementCallback
 * @see RedirectGravestonePositionCallback
 * @see PositionValidationCallback
 * @see InsertGravestoneItemCallback
 * @see GravestonePlacedCallback
 */
public class GravestonesApi {
    private static final Map<Identifier, GravestoneDataType> DATA_TYPES = new HashMap<>();
    private static final List<BiPredicate<PlayerEntity, ItemStack>> ITEM_SKIP_PREDICATES = new ArrayList<>();

    /**
     * Registers a type of data that gravestones save, and how gravestones should handle that data.
     */
    public static void registerDataType(Identifier identifier, GravestoneDataType dataType) {
        DATA_TYPES.put(identifier, dataType);
    }

    /**
     * @deprecated Use {@link InsertGravestoneItemCallback#EVENT} instead.
     */
    @Deprecated
    public static void registerItemSkipPredicate(BiPredicate<PlayerEntity, ItemStack> predicate) {
        ITEM_SKIP_PREDICATES.add(predicate);
    }

    /**
     * Removes data from the player, and returns an NBT Compound with that data.
     */
    public static NbtCompound getDataToInsert(PlayerEntity player) {
        NbtCompound contents = new NbtCompound();

        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            NbtCompound view = new NbtCompound();
            entry.getValue().writeData(view, player);

            contents.put(
                    entry.getKey().toString(),
                    view
            );
        }

        return contents;
    }

    /**
     * Called when gravestones are broken, including when collected.
     */
    public static void onBreak(World world, BlockPos pos, int decay, TechnicalGravestoneBlockEntity entity) {
        onBreak(world, pos, decay, entity.getContents());
    }

    public static void onBreak(World world, BlockPos pos, int decay, NbtCompound contents) {
        if (contents.isEmpty()) return;

        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            entry.getValue().onBreak(
                    contents.getCompound(entry.getKey().toString()),
                    world,
                    pos,
                    decay
            );
        }
    }

    /**
     * Called when gravestones are collected.
     */
    public static void onCollect(World world, BlockPos pos, PlayerEntity player, int decay, NbtCompound contents) {
        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            entry.getValue().onCollect(
                    contents.getCompound(entry.getKey().toString()),
                    world,
                    pos,
                    player,
                    decay
            );
        }
    }

    /**
     * @return Whether gravestone decay should affect gameplay.
     */
    public static boolean shouldDecayAffectGameplay() {
        return !GravestonesConfig.AESTHETIC_DECAY.getValue();
    }

    /**
     * Checks all registered {@link InsertGravestoneItemCallback} listeners, and item skip predicates,
     * to see whether an item stack should be skipped by gravestone processing.
     *
     * <p>This should be called before checking anything else about the stack,
     * as listeners may change it (e.g. emptying it).
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @return Whether the item should be skipped.
     * @see InsertGravestoneItemCallback#EVENT
     */
    public static boolean shouldSkipItem(PlayerEntity player, ItemStack stack) {
        if (InsertGravestoneItemCallback.EVENT.invoker().insertItem(player, stack)) {
            return true;
        }

        for (BiPredicate<PlayerEntity, ItemStack> predicate : ITEM_SKIP_PREDICATES) {
            if (predicate.test(player, stack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * If the Experience Decay config is enabled, and gravestone decay should affect gameplay,
     * applies experience decay to an amount of experience.
     * Otherwise, does nothing.
     *
     * @param experience The initial experience amount
     * @param decay The decay stage of the gravestone
     * @return The final (decayed) experience amount
     */
    public static int getDecayedExperience(int experience, int decay) {
        if (GravestonesConfig.EXPERIENCE_DECAY.getValue() && shouldDecayAffectGameplay()) {
            return experience / (decay + 1);
        } else {
            return experience;
        }
    }
}
