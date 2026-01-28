package net.pneumono.gravestones.compat;

//? if resource_backpacks {
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    public void writeData(NbtCompound nbt, DynamicOps<NbtElement> ops, PlayerEntity player) {
        if (player instanceof BackpackHolder backpackHolder) {
            ItemStack stack = backpackHolder.getBackpack();

            GravestonesApi.onInsertItem(player, stack, Identifier.of("resource_backpacks", KEY));
            if (GravestonesApi.shouldSkipItem(player, stack)) return;

            VersionUtil.put(ops, nbt, KEY, ItemStack.CODEC, stack);

            backpackHolder.setBackpack(ItemStack.EMPTY);
        }
    }

    @Override
    public void onBreak(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, int decay) {
        Optional<ItemStack> optional = VersionUtil.get(ops, nbt, KEY, ItemStack.CODEC);
        optional.ifPresent(stack -> dropStack(world, pos, stack));
    }

    @Override
    public void onCollect(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, PlayerEntity player, int decay) {
        Optional<ItemStack> optional = VersionUtil.get(ops, nbt, KEY, ItemStack.CODEC);
        optional.ifPresent(stack -> {
            if (player instanceof BackpackHolder backpackHolder && backpackHolder.getBackpack().isEmpty()) {
                for (Slot slot : player.playerScreenHandler.slots) {
                    if (slot instanceof BackpackSlot) {
                        slot.setStackNoCallbacks(stack);
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