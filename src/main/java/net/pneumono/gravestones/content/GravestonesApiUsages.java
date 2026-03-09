package net.pneumono.gravestones.content;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.SkipItemCallback;
import net.pneumono.gravestones.multiversion.VersionUtil;

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
 * Contains usages of the Gravestones API by Gravestones itself. These can be used as examples if necessary.
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

        SkipItemCallback.EVENT.register((player, itemStack, slot) ->
                itemStack.is(GravestonesApi.ITEM_SKIPS_GRAVESTONES) ||
                //? if >=1.21 {
                EnchantmentHelper.hasTag(itemStack, GravestonesApi.ENCHANTMENT_SKIPS_GRAVESTONES)
                //?} else {
                /*hasTagSkippableEnchantments(itemStack)
                *///?}
        );
        SkipItemCallback.EVENT.register((player, itemStack, slot) -> hasSkippedEnchantments(itemStack));
        SkipItemCallback.EVENT.register((player, itemStack, slot) ->
                //? if >=1.21 {
                EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                //?} else {
                /*EnchantmentHelper.hasVanishingCurse(itemStack)
                *///?}
        );

        CancelGravestonePlacementCallback.EVENT.register((level, player, deathPos) -> {
            //? if >=1.21.11 {
            boolean keepInv = level.getGameRules().get(GameRules.KEEP_INVENTORY);
            //?} else {
            /*boolean keepInv = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            *///?}
            return keepInv && !GravestonesConfig.SPAWN_GRAVESTONES_WITH_KEEPINV.getValue();
        });
        CancelGravestonePlacementCallback.EVENT.register((level, player, deathPos) ->
                player.isCreative() && !GravestonesConfig.SPAWN_GRAVESTONES_IN_CREATIVE.getValue()
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
