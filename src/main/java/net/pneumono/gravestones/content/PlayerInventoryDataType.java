package net.pneumono.gravestones.content;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

import java.util.*;

public class PlayerInventoryDataType extends GravestoneDataType {
    @Override
    public NbtElement getDataToInsert(PlayerEntity player) {
        NbtCompound nbt = new NbtCompound();
        PlayerInventory inventory = player.getInventory();

        nbt.put("inventory", inventoryToNbtList(inventory, 36, 0));
        nbt.put("equipment", inventoryToNbtList(inventory, 5, 36));

        return nbt;
    }

    public NbtList inventoryToNbtList(PlayerInventory inventory, int amount, int offset) {
        PlayerEntity player = inventory.player;
        NbtList list = new NbtList();

        for (int i = 0; i < amount; i++) {
            ItemStack stack = inventory.getStack(i + offset);
            if (!stack.isEmpty() && !GravestonesApi.shouldSkipItem(player, stack)) {
                NbtCompound compound = new NbtCompound();
                compound.putByte("Slot", (byte)i);

                list.add(inventory.removeStack(i + offset).toNbt(player.getRegistryManager(), compound));
            }
        }

        return list;
    }

    @Override
    public void onBreak(World world, BlockPos pos, int decay, NbtElement element) {
        if (element == null) return;
        Optional<NbtCompound> optional = element.asCompound();
        if (optional.isEmpty()) return;
        NbtCompound nbt = optional.get();

        Collection<ItemStack> items = inventoryFromNbt(nbt, world.getRegistryManager()).values();

        for (ItemStack stack : items) {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    @Override
    public void onCollect(PlayerEntity player, int decay, NbtElement element) {
        if (element == null) return;
        Optional<NbtCompound> optional = element.asCompound();
        if (optional.isEmpty()) return;
        NbtCompound nbt = optional.get();

        Map<Integer, ItemStack> items = inventoryFromNbt(nbt, player.getRegistryManager());

        List<ItemStack> additionalItems = new ArrayList<>();
        PlayerInventory inventory = player.getInventory();
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            int slot = entry.getKey();
            ItemStack stack = entry.getValue();

            if (inventory.getStack(slot).isEmpty()) {
                inventory.setStack(slot, stack);
            } else {
                additionalItems.add(stack);
            }
        }

        for (ItemStack stack : additionalItems) {
            if (!player.giveItemStack(stack)) {
                ItemEntity itemEntity = player.dropItem(stack, false);
                if (itemEntity != null) {
                    itemEntity.resetPickupDelay();
                    itemEntity.setOwner(player.getUuid());
                }
            }
        }
    }

    public static Map<Integer, ItemStack> inventoryFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        Map<Integer, ItemStack> map = new HashMap<>();

        Optional<NbtList> inventoryOptional = nbt.getList("inventory");
        inventoryOptional.ifPresent(nbtList -> map.putAll(subInventoryFromNbtList(nbtList, registries, 0)));

        Optional<NbtList> equipmentOptional = nbt.getList("equipment");
        equipmentOptional.ifPresent(nbtList -> map.putAll(subInventoryFromNbtList(nbtList, registries, 36)));

        return map;
    }

    private static Map<Integer, ItemStack> subInventoryFromNbtList(NbtList nbtList, RegistryWrapper.WrapperLookup registries, int slotOffset) {
        Map<Integer, ItemStack> map = new HashMap<>();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompoundOrEmpty(i);

            int slot = nbtCompound.getByte("Slot", (byte)0) & 255;
            ItemStack stack = ItemStack.fromNbt(registries, nbtCompound).orElse(ItemStack.EMPTY);

            if (!stack.isEmpty()) {
                map.put(slot + slotOffset, stack);
            }
        }

        return map;
    }
}
