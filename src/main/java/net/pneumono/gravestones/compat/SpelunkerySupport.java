package net.pneumono.gravestones.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.pneumono.gravestones.api.ModSupport;

public class SpelunkerySupport extends ModSupport {
    @Override
    public boolean shouldPutItemInGravestone(PlayerEntity player, ItemStack stack) {
        return !stack.isOf(Items.RECOVERY_COMPASS);
    }
}
