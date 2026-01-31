package net.pneumono.gravestones.content;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.SkipItemCallback;

import java.util.Optional;

//? if >=1.21.11 {
import net.minecraft.world.rule.GameRules;
//?} else {
/*import net.minecraft.world.GameRules;
*///?}

//? if >=1.21 {
import net.minecraft.component.EnchantmentEffectComponentTypes;
//?}

//? if <1.20.5 {
/*import net.minecraft.registry.Registries;
*///?}

/**
 * Contains usages of the Gravestones API by Gravestones itself. These can be used as examples if necessary.
 */
public class GravestonesApiUsages {
    public static void register() {
        GravestonesApi.registerDataType(Gravestones.id("inventory"), new PlayerInventoryDataType());
        GravestonesApi.registerDataType(Gravestones.id("experience"), new ExperienceDataType());

        GravestonesApi.addSkippedEnchantment(Identifier.of("enderio", "soulbound"));
        GravestonesApi.addSkippedEnchantment(Identifier.of("alessandrvenchantments", "soulbound"));
        GravestonesApi.addSkippedEnchantment(Identifier.of("enchantery", "soulbound"));
        GravestonesApi.addSkippedEnchantment(Identifier.of("enderzoology", "soulbound"));
        GravestonesApi.addSkippedEnchantment(Identifier.of("soulbound", "soulbound"));
        GravestonesApi.addSkippedEnchantment(Identifier.of("soulbound_enchantment", "soulbound"));

        SkipItemCallback.EVENT.register((player, itemStack, slot) ->
                itemStack.isIn(GravestonesApi.ITEM_SKIPS_GRAVESTONES) ||
                //? if >=1.21 {
                EnchantmentHelper.hasAnyEnchantmentsIn(itemStack, GravestonesApi.ENCHANTMENT_SKIPS_GRAVESTONES)
                //?} else {
                /*hasTagSkippableEnchantments(itemStack)
                *///?}
        );
        SkipItemCallback.EVENT.register((player, itemStack, slot) -> hasSkippedEnchantments(itemStack));
        SkipItemCallback.EVENT.register((player, itemStack, slot) ->
                //? if >=1.21 {
                EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)
                //?} else {
                /*EnchantmentHelper.hasVanishingCurse(itemStack)
                *///?}
        );

        CancelGravestonePlacementCallback.EVENT.register((world, player, deathPos) -> {
            //? if >=1.21.11 {
            boolean keepInv = world.getGameRules().getValue(GameRules.KEEP_INVENTORY);
            //?} else {
            /*boolean keepInv = world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
            *///?}
            return keepInv && !GravestonesConfig.SPAWN_GRAVESTONES_WITH_KEEPINV.getValue();
        });
        CancelGravestonePlacementCallback.EVENT.register((world, player, deathPos) ->
                player.isCreative() && !GravestonesConfig.SPAWN_GRAVESTONES_IN_CREATIVE.getValue()
        );
    }

    //? if <1.20.5 {
    /*@SuppressWarnings("deprecation")
     *///?}
    //? if <1.21 {
    /*private static boolean hasTagSkippableEnchantments(ItemStack stack) {
        //? if >=1.20.5 {
        for (RegistryEntry<Enchantment> enchantment : stack.getEnchantments().getEnchantments()) {
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

    private static boolean hasSkippedEnchantments(ItemStack stack) {
        //? if >=1.20.5 {
        for (RegistryEntry<Enchantment> enchantment : stack.getEnchantments().getEnchantments()
        ) {
            Optional<RegistryKey<Enchantment>> optional = enchantment.getKey();
            if (optional.isPresent() && GravestonesApi.isSkippedEnchantment(optional.get().getValue())) return true;
        }
        return false;
        //?} else {
        /*for (RegistryEntry<Enchantment> enchantment : EnchantmentHelper.get(stack).keySet().stream()
                .map(Registries.ENCHANTMENT::getEntry)
                .toList()
        ) {
            Optional<RegistryKey<Enchantment>> optional = enchantment.getKey();
            if (optional.isPresent() && GravestonesApi.isSkippedEnchantment(optional.get().getValue())) return true;
        }
        return false;
        *///?}
    }
}
