package net.pneumono.gravestones.api;

import com.mojang.serialization.DynamicOps;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.multiversion.VersionUtil;
import org.jetbrains.annotations.Nullable;

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
 * {@link SkipItemCallback} and {@link OnInsertItemCallback} (for items with different behavior on death)
 * and {@link #registerDataType} (for custom data that also needs to be saved on death).
 *
 * <p>There is a page on the <a href="https://github.com/PneumonoIsNotAvailable/Gravestones/wiki">Gravestones Wiki</a>
 * for Gravestones' API features, however the documentation here is more detailed and likely to be up-to-date.
 *
 * @see GravestoneDataType
 * @see CancelGravestonePlacementCallback
 * @see RedirectGravestonePositionCallback
 * @see PositionValidationCallback
 * @see SkipItemCallback
 * @see OnInsertItemCallback
 * @see GravestonePlacedCallback
 * @see GravestoneCollectedCallback
 */
public class GravestonesApi {
    private static final Map<Identifier, GravestoneDataType> DATA_TYPES = new HashMap<>();
    private static final List<BiPredicate<PlayerEntity, ItemStack>> ITEM_SKIP_PREDICATES = new ArrayList<>();

    public static final TagKey<Item> ITEM_SKIPS_GRAVESTONES = TagKey.of(RegistryKeys.ITEM, Gravestones.id("skips_gravestones"));
    public static final TagKey<Enchantment> ENCHANTMENT_SKIPS_GRAVESTONES = TagKey.of(RegistryKeys.ENCHANTMENT, Gravestones.id("skips_gravestones"));
    public static final TagKey<Block> BLOCK_GRAVESTONE_IRREPLACEABLE = TagKey.of(RegistryKeys.BLOCK, Gravestones.id("gravestone_irreplaceable"));

    /**
     * Registers a type of data that gravestones save, and how gravestones should handle that data.
     */
    public static void registerDataType(Identifier identifier, GravestoneDataType dataType) {
        DATA_TYPES.put(identifier, dataType);
    }

    /**
     * @deprecated Use {@link SkipItemCallback#EVENT} instead.
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
            NbtCompound data = new NbtCompound();
            try {
                DynamicOps<NbtElement> ops = /*? if >=1.20.5 {*/player.getRegistryManager().getOps(NbtOps.INSTANCE)/*?} else {*//*NbtOps.INSTANCE*//*?}*/;
                entry.getValue().writeData(data, ops, player);
            } catch (Exception e) {
                Gravestones.LOGGER.error("Gravestones Data Type '{}' failed to write data:", entry.getKey().toString(), e);
            }

            contents.put(
                    entry.getKey().toString(),
                    data
            );
        }

        return contents;
    }

    /**
     * Called when gravestones are broken, including when collected.
     */
    public static void onBreak(ServerWorld world, BlockPos pos, int decay, TechnicalGravestoneBlockEntity entity) {
        onBreak(world, pos, decay, entity.getContents());
    }

    /**
     * Called when gravestones are broken, including when collected.
     */
    public static void onBreak(ServerWorld world, BlockPos pos, int decay, NbtCompound contents) {
        if (contents.isEmpty()) return;

        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            String key = entry.getKey().toString();
            try {
                DynamicOps<NbtElement> ops = /*? if >=1.20.5 {*/world.getRegistryManager().getOps(NbtOps.INSTANCE)/*?} else {*//*NbtOps.INSTANCE*//*?}*/;
                entry.getValue().onBreak(
                        VersionUtil.getCompoundOrEmpty(contents, key),
                        ops,
                        world,
                        pos,
                        decay
                );
            } catch (Exception e) {
                Gravestones.LOGGER.error("Gravestones Data Type '{}' failed to drop contents:", key, e);
            }
        }
    }

    /**
     * Called when gravestones are collected.
     */
    public static void onCollect(ServerWorld world, BlockPos pos, PlayerEntity player, int decay, NbtCompound contents) {
        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {

            String key = entry.getKey().toString();
            try {
                DynamicOps<NbtElement> ops = /*? if >=1.20.5 {*/world.getRegistryManager().getOps(NbtOps.INSTANCE)/*?} else {*//*NbtOps.INSTANCE*//*?}*/;
                entry.getValue().onCollect(
                        VersionUtil.getCompoundOrEmpty(contents, key),
                        ops,
                        world,
                        pos,
                        player,
                        decay
                );
                contents.remove(key);
            } catch (Exception e) {
                Gravestones.LOGGER.error("Gravestones Data Type '{}' failed to return contents:", key, e);
            }
        }
    }

    /**
     * @return Whether gravestone decay should affect gameplay.
     */
    public static boolean shouldDecayAffectGameplay() {
        return !GravestonesConfig.AESTHETIC_DECAY.getValue();
    }

    /**
     * Checks all registered {@link SkipItemCallback} listeners,
     * to see whether an item stack should be skipped by gravestone processing.
     *
     * <p>This should be called after {@link #onInsertItem}, as listeners may change the stack.
     *
     * <p>If the item comes from a specific slot,
     * {@link #shouldSkipItem(PlayerEntity, ItemStack, Identifier)} should be used instead.
     *
     * <p>Also checks {@link InsertGravestoneItemCallback} listeners and Item Skip Predicates for backwards compatibility.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @return Whether the item should be skipped.
     * @see SkipItemCallback#EVENT
     */
    @SuppressWarnings("deprecation")
    public static boolean shouldSkipItem(PlayerEntity player, ItemStack stack) {
        return shouldSkipItem(player, stack, null);
    }

    /**
     * Checks all registered {@link SkipItemCallback} listeners,
     * to see whether an item stack should be skipped by gravestone processing.
     *
     * <p>This should be called after {@link #onInsertItem}, as that may make changes the stack.
     *
     * <p>Slot identifiers should always use the mod they originate from as a namespace,
     * to maintain compatibility with other mods.
     * Slot identifiers do not need to match perfectly to the actual slots themselves,
     * and simply need to be unique for each slot.
     *
     * <p>Also checks {@link InsertGravestoneItemCallback} listeners and Item Skip Predicates for backwards compatibility.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @param slot The identifier of the slot holding the item stack.
     * @return Whether the item should be skipped.
     * @see SkipItemCallback#EVENT
     */
    @SuppressWarnings("deprecation")
    public static boolean shouldSkipItem(PlayerEntity player, ItemStack stack, @Nullable Identifier slot) {
        if (InsertGravestoneItemCallback.EVENT.invoker().insertItem(player, stack)) {
            return true;
        }

        if (SkipItemCallback.EVENT.invoker().insertItem(player, stack, slot)) {
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
     * Invokes all {@link OnInsertItemCallback} listeners.
     *
     * <p>Should be called for each item stack before being inserted into a gravestone,
     * including ones that will be skipped.
     *
     * <p>If the item comes from a specific slot,
     * {@link #onInsertItem(PlayerEntity, ItemStack, Identifier)} should be used instead.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @see OnInsertItemCallback#EVENT
     */
    public static void onInsertItem(PlayerEntity player, ItemStack stack) {
        onInsertItem(player, stack, null);
    }

    /**
     * Invokes all {@link OnInsertItemCallback} listeners.
     *
     * <p>Should be called for each item stack before being inserted into a gravestone,
     * including ones that will be skipped.
     *
     * <p>Slot identifiers should always use the mod they originate from as a namespace,
     * to maintain compatibility with other mods.
     * Slot identifiers do not need to match perfectly to the actual slots themselves,
     * and simply need to be unique for each slot.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @param slot The identifier of the slot holding the item stack.
     * @see OnInsertItemCallback#EVENT
     */
    public static void onInsertItem(PlayerEntity player, ItemStack stack, @Nullable Identifier slot) {
        OnInsertItemCallback.EVENT.invoker().insertItem(player, stack, slot);
    }

    /**
     * If the Experience Decay config is enabled, and gravestone decay should affect gameplay,
     * applies experience decay to an amount of experience.
     * Otherwise, does nothing.
     *
     * @param experience The initial experience amount.
     * @param decay The decay stage of the gravestone.
     * @return The final (decayed) experience amount.
     */
    public static int getDecayedExperience(int experience, int decay) {
        if (GravestonesConfig.EXPERIENCE_DECAY.getValue() && shouldDecayAffectGameplay()) {
            return experience / (decay + 1);
        } else {
            return experience;
        }
    }
}
