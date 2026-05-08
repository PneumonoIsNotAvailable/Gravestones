package net.pneumono.gravestones.compat;

//? if resource_backpacks {
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.multiversion.VersionUtil;
import net.xstopho.resource_backpacks.backpack.api.BackpackHolder;
import net.xstopho.resource_backpacks.client.slot.BackpackSlot;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ResourceBackpacksDataType extends GravestoneDataType {
    private static final String KEY = "backpack";

    @Override
    public void writeData(CompoundTag tag, DynamicOps<Tag> ops, Player player) {
        if (player instanceof BackpackHolder backpackHolder) {
            ItemStack stack = backpackHolder.getBackpack();

            GravestonesApi.onInsertItem(player, stack, Identifier.fromNamespaceAndPath("resource_backpacks", KEY));
            if (GravestonesApi.shouldSkipItem(player, stack)) return;

            VersionUtil.put(ops, tag, KEY, ItemStack.CODEC, stack);

            backpackHolder.setBackpack(ItemStack.EMPTY);
        }
    }

    @Override
    public void onBreak(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, int decay) {
        Optional<ItemStack> optional = VersionUtil.get(ops, tag, KEY, ItemStack.CODEC);
        optional.ifPresent(stack -> dropStack(level, pos, stack));
    }

    @Override
    public void onCollect(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, Player player, int decay) {
        Optional<ItemStack> optional = VersionUtil.get(ops, tag, KEY, ItemStack.CODEC);
        optional.ifPresent(stack -> {
            if (player instanceof BackpackHolder backpackHolder && backpackHolder.getBackpack().isEmpty()) {
                for (Slot slot : player.inventoryMenu.slots) {
                    if (slot instanceof BackpackSlot) {
                        slot.set(stack);
                        break;
                    }
                }
            } else {
                dropStack(player, stack);
            }
        });
    }
}
//?}