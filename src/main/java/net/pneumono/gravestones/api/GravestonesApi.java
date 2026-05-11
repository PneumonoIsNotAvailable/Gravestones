package net.pneumono.gravestones.api;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.event.GravestoneCollectionEvents;
import net.pneumono.gravestones.api.event.GravestoneContentsEvents;
import net.pneumono.gravestones.api.event.GravestonePlacementEvents;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.content.GravestonesApiUsages;
import net.pneumono.gravestones.gravestones.GravestoneManager;
import net.pneumono.gravestones.multiversion.VersionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
 * {@link GravestoneContentsEvents.SkipItem}, and {@link GravestoneContentsEvents.InsertItem} (for items with different behavior on death)
 * and {@link #registerDataType} (for custom data that also needs to be saved on death).
 *
 * <p>{@link GravestonesApiUsages} contains all the usages of the Gravestones API by Gravestones itself.
 * These can be used as examples if needed.
 *
 * <p>There is a page on the <a href="https://github.com/PneumonoIsNotAvailable/Gravestones/wiki">Gravestones Wiki</a>
 * for Gravestones' API features, however the documentation here is more detailed and more likely to be up-to-date.
 *
 * @see GravestoneDataType
 * @see GravestonePlacementEvents
 * @see GravestoneContentsEvents
 * @see GravestoneCollectionEvents
 */
public class GravestonesApi {
    private static final Map<Identifier, GravestoneDataType> DATA_TYPES = new HashMap<>();
    private static final List<BiPredicate<Player, ItemStack>> ITEM_SKIP_PREDICATES = new ArrayList<>();
    private static final List<Identifier> SKIPPED_ENCHANTMENTS = new ArrayList<>();

    public static final TagKey<Item> ITEM_SKIPS_GRAVESTONES = TagKey.create(Registries.ITEM, Gravestones.id("skips_gravestones"));
    public static final TagKey<Enchantment> ENCHANTMENT_SKIPS_GRAVESTONES = TagKey.create(Registries.ENCHANTMENT, Gravestones.id("skips_gravestones"));
    public static final TagKey<Block> BLOCK_GRAVESTONE_IRREPLACEABLE = TagKey.create(Registries.BLOCK, Gravestones.id("gravestone_irreplaceable"));

    /**
     * Registers a type of data that gravestones save, and how gravestones should handle that data.
     */
    public static void registerDataType(Identifier identifier, GravestoneDataType dataType) {
        DATA_TYPES.put(identifier, dataType);
    }

    /**
     * @deprecated Use {@link GravestoneContentsEvents.SkipItem} instead.
     */
    @Deprecated(forRemoval = true)
    public static void registerItemSkipPredicate(BiPredicate<Player, ItemStack> predicate) {
        ITEM_SKIP_PREDICATES.add(predicate);
    }

    /**
     * Adds an enchantment to a list of enchantments gravestones should skip.
     *
     * <p>This is intended for use in versions where enchantment tags are not supported,
     * and so {@code gravestones:skips_gravestones} cannot be used.
     * However, this will still work regardless of version.
     */
    public static void addSkippedEnchantment(Holder<Enchantment> enchantment) {
        Optional<ResourceKey<Enchantment>> optional = enchantment.unwrapKey();
        if (optional.isPresent()) {
            addSkippedEnchantment(optional.get());
        } else {
            throw new IllegalArgumentException("Cannot register a skipped enchantment that has no key");
        }
    }

    /**
     * Adds an enchantment to a list of enchantments gravestones should skip.
     *
     * <p>This is intended for use in versions where enchantment tags are not supported,
     * and so {@code gravestones:skips_gravestones} cannot be used.
     * However, this will still work regardless of version.
     */
    public static void addSkippedEnchantment(ResourceKey<Enchantment> enchantment) {
        addSkippedEnchantment(VersionUtil.getId(enchantment));
    }

    /**
     * Adds an enchantment to a list of enchantments gravestones should skip.
     *
     * <p>This is intended for use in versions where enchantment tags are not supported,
     * and so {@code gravestones:skips_gravestones} cannot be used.
     * However, this will still work regardless of version.
     */
    public static void addSkippedEnchantment(Identifier id) {
        SKIPPED_ENCHANTMENTS.add(id);
    }

    public static boolean isSkippedEnchantment(Identifier checked) {
        for (Identifier skipped : SKIPPED_ENCHANTMENTS) {
            if (skipped.equals(checked)) return true;
        }
        return false;
    }

    /**
     * Removes data from the player, and returns a CompoundTag with that data.
     */
    public static CompoundTag getDataToInsert(Player player) {
        CompoundTag contents = new CompoundTag();

        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            String key = entry.getKey().toString();
            GravestoneManager.info("Creating data for Data Type '{}'...", key);
            CompoundTag data = new CompoundTag();
            try {
                DynamicOps<Tag> ops = /*? if >=1.20.5 {*/player.registryAccess().createSerializationContext(NbtOps.INSTANCE)/*?} else {*//*NbtOps.INSTANCE*//*?}*/;
                entry.getValue().writeData(data, ops, player);
            } catch (Exception e) {
                Gravestones.LOGGER.error("Gravestone Data Type '{}' failed to write data:", key, e);
            }

            contents.put(
                    key,
                    data
            );
        }

        return contents;
    }

    /**
     * Called when gravestones are broken, including when collected.
     *
     * @return {@code true} if no errors were thrown, {@code false} otherwise.
     */
    public static boolean onBreak(ServerLevel level, BlockPos pos, int decay, TechnicalGravestoneBlockEntity entity) {
        return onBreak(level, pos, decay, entity.getContents());
    }

    /**
     * Called when gravestones are broken, including when collected.
     *
     * @return {@code true} if no errors were thrown, {@code false} otherwise.
     */
    public static boolean onBreak(ServerLevel level, BlockPos pos, int decay, CompoundTag contents) {
        boolean success = true;

        if (contents.isEmpty()) return true;

        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            String key = entry.getKey().toString();
            GravestoneManager.info("Processing data for Gravestone Data Type '{}'...", key);
            try {
                DynamicOps<Tag> ops = /*? if >=1.20.5 {*/level.registryAccess().createSerializationContext(NbtOps.INSTANCE)/*?} else {*//*NbtOps.INSTANCE*//*?}*/;
                entry.getValue().onBreak(
                        VersionUtil.getCompoundOrEmpty(contents, key),
                        ops,
                        level,
                        pos,
                        decay
                );
                contents.remove(key);
            } catch (Exception e) {
                Gravestones.LOGGER.error("Gravestone Data Type '{}' failed to drop contents:", key, e);
                success = false;
            }
        }

        if (!contents.isEmpty()) {
            Gravestones.LOGGER.warn("Some Gravestone Data could not be dropped by registered Gravestone Data Types: {}", contents);
        }

        return success;
    }

    /**
     * Called when gravestones are collected.
     *
     * @return {@code true} if no errors were thrown, {@code false} otherwise.
     */
    public static boolean onCollect(ServerLevel level, BlockPos pos, Player player, int decay, CompoundTag contents) {
        boolean success = true;

        if (contents.isEmpty()) return true;

        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            String key = entry.getKey().toString();
            GravestoneManager.info("Processing data for Gravestone Data Type '{}'...", key);
            try {
                DynamicOps<Tag> ops = /*? if >=1.20.5 {*/level.registryAccess().createSerializationContext(NbtOps.INSTANCE)/*?} else {*//*NbtOps.INSTANCE*//*?}*/;
                entry.getValue().onCollect(
                        VersionUtil.getCompoundOrEmpty(contents, key),
                        ops,
                        level,
                        pos,
                        player,
                        decay
                );
                contents.remove(key);
            } catch (Exception e) {
                Gravestones.LOGGER.error("Gravestone Data Type '{}' failed to return contents:", key, e);
                success = false;
            }
        }

        if (!contents.isEmpty()) {
            Gravestones.LOGGER.warn("Some Gravestone Data could not be collected by registered Gravestone Data Types: {}", contents);
        }

        return success;
    }

    /**
     * @return Whether gravestone decay should affect gameplay.
     */
    public static boolean shouldDecayAffectGameplay() {
        return !GravestonesConfig.AESTHETIC_DECAY.getValue();
    }

    /**
     * Invokes all {@link GravestoneContentsEvents.SkipItem} listeners,
     * to see whether an item stack should be skipped by gravestone processing.
     *
     * <p>This should be called after {@link #onInsertItem}, as listeners may change the stack.
     *
     * <p>If the item comes from a specific slot,
     * {@link #shouldSkipItem(Player, ItemStack, Identifier)} should be used instead.
     *
     * <p>Also invokes deprecated event listeners and Item Skip Predicates for backwards compatibility.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @return Whether the item should be skipped.
     * @see GravestoneContentsEvents.SkipItem
     */
    public static boolean shouldSkipItem(Player player, ItemStack stack) {
        return shouldSkipItem(player, stack, null);
    }

    /**
     * Checks all {@link GravestoneContentsEvents.SkipItem} listeners,
     * to see whether an item stack should be skipped by gravestone processing.
     *
     * <p>This should be called after {@link #onInsertItem}, as listeners may change the stack.
     *
     * <p>Slot identifiers should always use the mod they originate from as a namespace,
     * to maintain compatibility with other mods.
     * Slot identifiers do not need to match perfectly to the actual slots themselves,
     * and simply need to be unique for each slot.
     *
     * <p>Also invokes deprecated event listeners and Item Skip Predicates for backwards compatibility.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @param slot The identifier of the slot holding the item stack.
     * @return Whether the item should be skipped.
     * @see GravestoneContentsEvents.SkipItem
     */
    public static boolean shouldSkipItem(Player player, ItemStack stack, @Nullable Identifier slot) {
        if (GravestoneContentsEvents.invokeSkipItem(player, stack, slot)) {
            return true;
        }

        if (DeprecatedEventHandler.insertGravestoneItemCallback(player, stack)) {
            return true;
        }

        if (DeprecatedEventHandler.skipItemCallback(player, stack, slot)) {
            return true;
        }

        for (BiPredicate<Player, ItemStack> predicate : ITEM_SKIP_PREDICATES) {
            if (predicate.test(player, stack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Invokes all {@link GravestoneContentsEvents.InsertItem} listeners.
     *
     * <p>Should be called for each item stack before being inserted into a gravestone,
     * including ones that will be skipped.
     *
     * <p>If the item comes from a specific slot,
     * {@link #onInsertItem(Player, ItemStack, Identifier)} should be used instead.
     *
     * <p>Also invokes deprecated event listeners, for backwards compatibility.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @see GravestoneContentsEvents.InsertItem
     */
    public static void onInsertItem(Player player, ItemStack stack) {
        onInsertItem(player, stack, null);
    }

    /**
     * Invokes all {@link GravestoneContentsEvents.InsertItem} listeners.
     *
     * <p>Should be called for each item stack before being inserted into a gravestone,
     * including ones that will be skipped.
     *
     * <p>Slot identifiers should always use the mod they originate from as a namespace,
     * to maintain compatibility with other mods.
     * Slot identifiers do not need to match perfectly to the actual slots themselves,
     * and simply need to be unique for each slot.
     *
     * <p>Also invokes deprecated event listeners, for backwards compatibility.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @param slot The identifier of the slot holding the item stack.
     * @see GravestoneContentsEvents.InsertItem
     */
    public static void onInsertItem(Player player, ItemStack stack, @Nullable Identifier slot) {
        GravestoneContentsEvents.invokeInsertItem(player, stack, slot);
        DeprecatedEventHandler.onInsertItemCallback(player, stack, slot);
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
