package net.pneumono.gravestones.content;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.GameRules;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.SkipItemCallback;

//? if >=1.21 {
import net.minecraft.component.EnchantmentEffectComponentTypes;
//?} else if >=1.20.5 {
/*import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
*///?} else {
/*import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
*///?}

/**
 * Contains usages of the Gravestones API by Gravestones itself. These can be used as examples if necessary.
 */
public class GravestonesApiUsages {
    public static void register() {
        GravestonesApi.registerDataType(Gravestones.id("inventory"), new PlayerInventoryDataType());
        GravestonesApi.registerDataType(Gravestones.id("experience"), new ExperienceDataType());

        SkipItemCallback.EVENT.register((player, itemStack, slot) ->
                itemStack.isIn(GravestonesApi.ITEM_SKIPS_GRAVESTONES) ||
                //? if >=1.21 {
                EnchantmentHelper.hasAnyEnchantmentsIn(itemStack, GravestonesApi.ENCHANTMENT_SKIPS_GRAVESTONES)
                //?} else {
                /*hasSkippableEnchantments(itemStack)
                *///?}
        );
        SkipItemCallback.EVENT.register((player, itemStack, slot) ->
                //? if >=1.21 {
                EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)
                //?} else {
                /*EnchantmentHelper.hasVanishingCurse(itemStack)
                *///?}
        );

        CancelGravestonePlacementCallback.EVENT.register((world, player, deathPos) ->
                world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && !GravestonesConfig.SPAWN_GRAVESTONES_WITH_KEEPINV.getValue()
        );
        CancelGravestonePlacementCallback.EVENT.register((world, player, deathPos) ->
                player.isCreative() && !GravestonesConfig.SPAWN_GRAVESTONES_IN_CREATIVE.getValue()
        );
    }

    //? if <1.20.5 {
    /*@SuppressWarnings("deprecation")
     *///?}
    //? if <1.21 {
    /*private static boolean hasSkippableEnchantments(ItemStack stack) {
        //? if >=1.20.5 {
        ItemEnchantmentsComponent component = stack.getEnchantments();
        for (RegistryEntry<Enchantment> enchantment : component.getEnchantments()) {
            if (enchantment.isIn(GravestonesApi.ENCHANTMENT_SKIPS_GRAVESTONES)) {
                return true;
            }
        }
        return false;
        //?} else {
        /^for (RegistryEntry<Enchantment> enchantment : EnchantmentHelper.get(stack).keySet().stream()
                .map(Registries.ENCHANTMENT::getEntry)
                .toList()
        ) {
            if (enchantment.isIn(GravestonesApi.ENCHANTMENT_SKIPS_GRAVESTONES)) {
                return true;
            }
        }
        return false;
        ^///?}
    }
    *///?}
}
