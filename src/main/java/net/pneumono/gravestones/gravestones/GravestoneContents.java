package net.pneumono.gravestones.gravestones;

import net.minecraft.block.BlockState;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class GravestoneContents extends GravestonesManager {
    public static void insertPlayerItemsAndExperience(TechnicalGravestoneBlockEntity gravestone, PlayerEntity player) {
        info("Inserting Inventory items and experience into grave...");
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (shouldSkipItem(player, stack)) {
                continue;
            }

            gravestone.setStack(i, inventory.removeStack(i));
        }

        info("Items inserted!");

        if (GravestonesConfig.STORE_EXPERIENCE.getValue()) {
            int experience = GravestonesConfig.EXPERIENCE_KEPT.getValue().calculateExperienceKept(player);
            if (GravestonesConfig.EXPERIENCE_CAP.getValue() && experience > 100) {
                experience = 100;
            }

            gravestone.setExperience(experience);
            player.experienceProgress = 0;
            player.experienceLevel = 0;
            player.totalExperience = 0;

            info("Experience inserted!");
        } else {
            info("Experience storing is disabled!");
        }
    }

    public static boolean shouldSkipItem(PlayerEntity player, ItemStack stack) {
        return GravestonesApi.shouldSkipItem(player, stack) ||
                stack.isIn(GravestonesRegistry.ITEM_SKIPS_GRAVESTONES) ||
                EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP) ||
                EnchantmentHelper.hasAnyEnchantmentsIn(stack, GravestonesRegistry.ENCHANTMENT_SKIPS_GRAVESTONES);
    }

    public static void insertModData(PlayerEntity entity, TechnicalGravestoneBlockEntity gravestone) {
        info("Inserting additional mod data into grave...");

        gravestone.setContents(GravestonesApi.getDataToInsert(entity));

        info("Data inserted!");
    }

    public static void returnContentsToPlayer(World world, TechnicalGravestoneBlockEntity gravestone, PlayerEntity player, BlockPos pos, BlockState state) {
        PlayerInventory inventory = player.getInventory();

        List<ItemStack> extraStacks = new ArrayList<>();
        for (int i = 0; i < gravestone.size(); ++i) {
            if (gravestone.getStack(i) != null) {
                if (i < 41 && inventory.getStack(i).isEmpty()) {
                    inventory.setStack(i, gravestone.getStack(i).copy());
                } else {
                    extraStacks.add(gravestone.getStack(i).copy());
                }

                gravestone.removeStack(i);
            }
        }
        for (ItemStack stack : extraStacks) {
            if (!player.giveItemStack(stack)) {
                ItemEntity itemEntity = player.dropItem(stack, false);
                if (itemEntity != null) {
                    itemEntity.resetPickupDelay();
                    itemEntity.setOwner(player.getUuid());
                }
            }
        }

        if (world instanceof ServerWorld serverWorld) {
            ExperienceOrbEntity.spawn(serverWorld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), gravestone.getExperienceToDrop(state));
            gravestone.setExperience(0);
        }

        GravestonesApi.onCollect(player, gravestone.getDecay(), gravestone.getContents());
    }
}
