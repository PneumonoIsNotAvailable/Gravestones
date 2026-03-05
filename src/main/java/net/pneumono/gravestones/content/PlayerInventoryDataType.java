package net.pneumono.gravestones.content;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.StackWithSlot;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.List;

public class PlayerInventoryDataType extends GravestoneDataType {
    private static final String KEY = "inventory";

    @Override
    public void writeData(CompoundTag tag, DynamicOps<Tag> ops, Player player) {
        ListTag list = new ListTag();
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            Identifier slotIdentifier = VersionUtil.createId("minecraft", Integer.toString(i));
            GravestonesApi.onInsertItem(player, itemStack, slotIdentifier);
            if (!GravestonesApi.shouldSkipItem(player, itemStack, slotIdentifier) && !itemStack.isEmpty()) {
                DataResult<Tag> result = StackWithSlot.CODEC.encodeStart(ops, new StackWithSlot(i, inventory.removeItemNoUpdate(i)));
                list.add(result.result().orElseThrow());
            }
        }
        tag.put(KEY, list);
    }

    @Override
    public void onBreak(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, int decay) {
        ListTag list = VersionUtil.getCompoundListOrEmpty(tag, KEY);

        for (Tag element : list) {
            ItemStack stack = StackWithSlot.CODEC.decode(ops, element).result().orElseThrow().getFirst().stack();
            dropStack(level, pos, stack);
        }
    }

    @Override
    public void onCollect(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, Player player, int decay) {
        Inventory inventory = player.getInventory();
        ListTag list = VersionUtil.getCompoundListOrEmpty(tag, KEY);
        List<StackWithSlot> stacks = new ArrayList<>();
        for (Tag element : list) {
            stacks.add(StackWithSlot.CODEC.decode(ops, element).result().orElseThrow().getFirst());
        }

        List<ItemStack> remainingStacks = new ArrayList<>();
        for (StackWithSlot stackWithSlot : stacks) {
            int slot = stackWithSlot.slot();
            if (stackWithSlot.isValidSlot(inventory.getContainerSize())) {

                ItemStack stack = stackWithSlot.stack();
                if (stack.isEmpty()) continue;

                if (inventory.getItem(slot).isEmpty()) {
                    inventory.setItem(slot, stack);
                    continue;
                }
            }
            remainingStacks.add(stackWithSlot.stack());
        }

        dropStacks(player, remainingStacks);
    }
}
