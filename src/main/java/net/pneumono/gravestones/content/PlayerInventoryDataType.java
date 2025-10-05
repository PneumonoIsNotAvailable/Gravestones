package net.pneumono.gravestones.content;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.StackWithSlot;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.List;

public class PlayerInventoryDataType extends GravestoneDataType {
    private static final String KEY = "inventory";

    @Override
    public void writeData(NbtCompound nbt, DynamicOps<NbtElement> ops, PlayerEntity player) {
        NbtList list = new NbtList();
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (!GravestonesApi.shouldSkipItem(player, itemStack) && !itemStack.isEmpty()) {
                DataResult<NbtElement> result = StackWithSlot.CODEC.encodeStart(ops, new StackWithSlot(i, inventory.removeStack(i)));
                list.add(result.result().orElseThrow());
            }
        }
        nbt.put(KEY, list);
    }

    @Override
    public void onBreak(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, int decay) {
        NbtList list = VersionUtil.getCompoundListOrEmpty(nbt, KEY);

        for (NbtElement element : list) {
            ItemStack stack = StackWithSlot.CODEC.decode(ops, element).result().orElseThrow().getFirst().stack();
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    @Override
    public void onCollect(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, PlayerEntity player, int decay) {
        PlayerInventory inventory = player.getInventory();
        NbtList list = VersionUtil.getCompoundListOrEmpty(nbt, KEY);
        List<StackWithSlot> stacks = new ArrayList<>();
        for (NbtElement element : list) {
            stacks.add(StackWithSlot.CODEC.decode(ops, element).result().orElseThrow().getFirst());
        }

        List<ItemStack> remainingStacks = new ArrayList<>();
        for (StackWithSlot stackWithSlot : stacks) {
            int slot = stackWithSlot.slot();
            if (stackWithSlot.isValidSlot(inventory.size())) {

                ItemStack stack = stackWithSlot.stack();
                if (stack.isEmpty()) continue;

                if (inventory.getStack(slot).isEmpty()) {
                    inventory.setStack(slot, stack);
                    continue;
                }
            }
            remainingStacks.add(stackWithSlot.stack());
        }

        for (ItemStack stack : remainingStacks) {
            if (!player.giveItemStack(stack)) {
                ItemEntity itemEntity = player.dropItem(stack, false);
                if (itemEntity != null) {
                    itemEntity.resetPickupDelay();
                    itemEntity.setOwner(player.getUuid());
                }
            }
        }
    }
}
