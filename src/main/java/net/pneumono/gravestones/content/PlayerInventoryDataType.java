package net.pneumono.gravestones.content;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;

public class PlayerInventoryDataType extends GravestoneDataType {
    @Override
    public void writeData(WriteView view, PlayerEntity player) {
        WriteView.ListAppender<StackWithSlot> list = view.getListAppender("inventory", StackWithSlot.CODEC);
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty()) {
                list.add(new StackWithSlot(i, itemStack));
            }
        }
    }

    @Override
    public void onBreak(ReadView view, World world, BlockPos pos, int decay) {
        ReadView.TypedListReadView<StackWithSlot> list = view.getTypedListView("inventory", StackWithSlot.CODEC);

        list.stream().map(StackWithSlot::stack).forEach(stack -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }

    @Override
    public void onCollect(ReadView view, World world, BlockPos pos, PlayerEntity player, int decay) {
        PlayerInventory inventory = player.getInventory();
        ReadView.TypedListReadView<StackWithSlot> list = view.getTypedListView("inventory", StackWithSlot.CODEC);

        list.stream().filter(stackWithSlot -> {
            int slot = stackWithSlot.slot();
            if (stackWithSlot.isValidSlot(inventory.size())) {

                ItemStack stack = inventory.getStack(slot);
                if (stack.isEmpty()) return false;

                if (player.getInventory().getStack(slot).isEmpty()) {
                    player.getInventory().setStack(slot, stack);
                    return false;
                }
            }
            return true;
        }).forEach(stackWithSlot -> {
            ItemStack stack = stackWithSlot.stack();
            if (!player.giveItemStack(stack)) {
                ItemEntity itemEntity = player.dropItem(stack, false);
                if (itemEntity != null) {
                    itemEntity.resetPickupDelay();
                    itemEntity.setOwner(player.getUuid());
                }
            }
        });
    }
}
