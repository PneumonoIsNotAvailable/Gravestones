package net.pneumono.gravestones.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

public class PlayerInventoryDataType extends GravestoneDataType {
    @Override
    public void writeData(NbtCompound view, PlayerEntity player) {
        NbtList list = new NbtList();
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (!GravestonesApi.shouldSkipItem(player, itemStack) && !itemStack.isEmpty()) {
                DataResult<NbtElement> element = StackWithSlot.CODEC.encodeStart(
                        player.getRegistryManager().getOps(NbtOps.INSTANCE), new StackWithSlot(i, inventory.removeStack(i))
                );
                if (element.isSuccess()) {
                    list.add(element.getOrThrow());
                }
            }
        }

        view.put("inventory", list);
    }

    @Override
    public void onBreak(NbtCompound view, World world, BlockPos pos, int decay) {
        NbtList list = view.getList("inventory", NbtElement.COMPOUND_TYPE);

        list.stream()
                .map(element -> StackWithSlot.CODEC.decode(world.getRegistryManager().getOps(NbtOps.INSTANCE), element))
                .filter(DataResult::isSuccess)
                .map(result -> result.getOrThrow().getFirst())
                .map(StackWithSlot::stack)
                .forEach(stack -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }

    @Override
    public void onCollect(NbtCompound view, World world, BlockPos pos, PlayerEntity player, int decay) {
        PlayerInventory inventory = player.getInventory();
        NbtList list = view.getList("inventory", NbtElement.COMPOUND_TYPE);

        list.stream()
                .map(element -> StackWithSlot.CODEC.decode(player.getRegistryManager().getOps(NbtOps.INSTANCE), element))
                .filter(DataResult::isSuccess)
                .map(result -> result.getOrThrow().getFirst())
                .filter(stackWithSlot -> {
                    int slot = stackWithSlot.slot();
                    if (stackWithSlot.isValidSlot(inventory.size())) {

                        ItemStack stack = stackWithSlot.stack();
                        if (stack.isEmpty()) return false;

                        if (inventory.getStack(slot).isEmpty()) {
                            inventory.setStack(slot, stack);
                            return false;
                        }
                    }
                    return true;
                })
                .forEach(stackWithSlot -> {
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

    public record StackWithSlot(int slot, ItemStack stack) {
        public static final Codec<StackWithSlot> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(Codecs.UNSIGNED_BYTE.fieldOf("Slot").orElse(0).forGetter(StackWithSlot::slot), ItemStack.CODEC.fieldOf("ItemStack").forGetter(StackWithSlot::stack))
                        .apply(instance, StackWithSlot::new)
        );

        public boolean isValidSlot(int slots) {
            return this.slot >= 0 && this.slot < slots;
        }
    }
}
