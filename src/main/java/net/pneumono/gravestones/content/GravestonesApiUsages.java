package net.pneumono.gravestones.content;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.event.GravestoneContentsEvents;
import net.pneumono.gravestones.api.event.GravestonePlacementEvents;
import net.pneumono.gravestones.gravestones.GravestonePlacement;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.Objects;
import java.util.Optional;

//? if >=1.21.11 {
import net.minecraft.world.level.gamerules.GameRules;
//?} else {
/*import net.minecraft.world.level.GameRules;
*///?}

//? if >=1.21 {
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
//?}

//? if <1.20.5 {
/*import net.minecraft.core.registries.BuiltInRegistries;
*///?}

/**
 * Contains usages of the Gravestones API by Gravestones itself. These can be used as examples.
 */
public class GravestonesApiUsages {
    public static void register() {
        GravestonesApi.registerDataType(Gravestones.id("inventory"), new PlayerInventoryDataType());
        GravestonesApi.registerDataType(Gravestones.id("experience"), new ExperienceDataType());

        GravestonesApi.addSkippedEnchantment(VersionUtil.createId("enderio", "soulbound"));
        GravestonesApi.addSkippedEnchantment(VersionUtil.createId("alessandrvenchantments", "soulbound"));
        GravestonesApi.addSkippedEnchantment(VersionUtil.createId("enchantery", "soulbound"));
        GravestonesApi.addSkippedEnchantment(VersionUtil.createId("enderzoology", "soulbound"));
        GravestonesApi.addSkippedEnchantment(VersionUtil.createId("soulbound", "soulbound"));
        GravestonesApi.addSkippedEnchantment(VersionUtil.createId("soulbound_enchantment", "soulbound"));

        GravestoneContentsEvents.registerSkipItem(Gravestones.id("tag_skip"), (player, itemStack, slot) ->
                itemStack.is(GravestonesApi.ITEM_SKIPS_GRAVESTONES) ||
                //? if >=1.21 {
                EnchantmentHelper.hasTag(itemStack, GravestonesApi.ENCHANTMENT_SKIPS_GRAVESTONES)
                //?} else {
                /*hasTagSkippableEnchantments(itemStack)
                *///?}
        );
        GravestoneContentsEvents.registerSkipItem(Gravestones.id("skipped_enchantments"), (player, itemStack, slot) -> hasSkippedEnchantments(itemStack));
        GravestoneContentsEvents.registerSkipItem(Gravestones.id("curse_of_vanishing"), (player, itemStack, slot) ->
                //? if >=1.21 {
                EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                //?} else {
                /*EnchantmentHelper.hasVanishingCurse(itemStack)
                *///?}
        );

        GravestonePlacementEvents.registerRedirectPosition(
                Gravestones.id("move_to_nearby_free_space"),
                (server, player, pos) -> {
                    ServerLevel level = server.getLevel(pos.dimension());
                    if (level == null) return pos;

                    return GravestonePlacement.getPlacementPos(level, pos);
                }
        );

        GravestonePlacementEvents.registerValidatePosition(
                Gravestones.id("valid_block_properties"),
                (level, state, pos) -> {
                    Block block = state.getBlock();
                    return block.defaultDestroyTime() >= 0 &&
                            block.getExplosionResistance() < 3600000 &&
                            block != Blocks.VOID_AIR;
                }
        );
        GravestonePlacementEvents.registerValidatePosition(
                Gravestones.id("not_in_irreplaceable_tag"),
                (level, state, pos) -> !state.is(GravestonesApi.BLOCK_GRAVESTONE_IRREPLACEABLE)
        );

        GravestonePlacementEvents.registerCancelPlace(
                Gravestones.id("keep_inventory"),
                (server, player, pos) -> {
                    //? if >=26.1 {
                    boolean keepInv = server.getGameRules().get(GameRules.KEEP_INVENTORY);
                    //?} else if >=1.21.11 {
                    /*boolean keepInv = Objects.requireNonNull(server.getLevel(pos.dimension())).getGameRules().get(GameRules.KEEP_INVENTORY);
                    *///?} else {
                    /*boolean keepInv = server.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
                    *///?}
                    return keepInv && !GravestonesConfig.SPAWN_GRAVESTONES_WITH_KEEPINV.getValue();
                }
        );
        GravestonePlacementEvents.registerCancelPlace(
                Gravestones.id("creative_mode"),
                (server, player, pos) -> player.isCreative() && !GravestonesConfig.SPAWN_GRAVESTONES_IN_CREATIVE.getValue()
        );
    }

    //? if <1.21 {
    /*private static boolean hasTagSkippableEnchantments(ItemStack stack) {
        //? if >=1.21 {
        for (Holder<Enchantment> enchantment : stack.getEnchantments().keySet()) {
            if (enchantment.is(GravestonesApi.ENCHANTMENT_SKIPS_GRAVESTONES)) {
                return true;
            }
        }
        //?}
        return false;
    }
    *///?}

    private static boolean hasSkippedEnchantments(ItemStack stack) {
        //? if >=1.20.5 {
        for (Holder<Enchantment> enchantment : stack.getEnchantments().keySet()
        ) {
            Optional<ResourceKey<Enchantment>> optional = enchantment.unwrapKey();
            if (optional.isPresent() && GravestonesApi.isSkippedEnchantment(VersionUtil.getId(optional.get()))) return true;
        }
        return false;
        //?} else {
        /*for (Optional<ResourceKey<Enchantment>> enchantment : EnchantmentHelper.getEnchantments(stack).keySet().stream()
                .map(BuiltInRegistries.ENCHANTMENT::getResourceKey)
                .toList()
        ) {
            if (enchantment.isPresent() && GravestonesApi.isSkippedEnchantment(enchantment.get().location())) return true;
        }
        return false;
        *///?}
    }
}
