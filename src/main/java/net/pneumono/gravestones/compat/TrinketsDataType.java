package net.pneumono.gravestones.compat;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
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

public class TrinketsDataType extends GravestoneDataType {
    @Override
    public NbtElement getDataToInsert(PlayerEntity player) {
        NbtList list = new NbtList();

        TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinketComponent == null) return list;

        trinketComponent.forEach((slotReference, stack) -> {
            if (!stack.isEmpty() && !GravestonesApi.shouldSkipItem(player, stack)) {

                NbtCompound compound = new NbtCompound();
                compound.putString("Slot", slotReference.getId());
                list.add(stack.copyAndEmpty().encode(player.getRegistryManager(), compound));
            }
        });

        return list;
    }

    @Override
    public void onBreak(World world, BlockPos pos, int decay, NbtElement element) {
        if (!(element instanceof NbtList nbtList)) return;

        Collection<ItemStack> items = inventoryFromNbt(nbtList, world.getRegistryManager()).values();

        for (ItemStack stack : items) {
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    @Override
    public void onCollect(PlayerEntity player, int decay, NbtElement element) {
        if (!(element instanceof NbtList nbtList)) return;

        List<ItemStack> additionalItems = new ArrayList<>();
        Map<String, ItemStack> items = inventoryFromNbt(nbtList, player.getRegistryManager());

        TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (trinketComponent != null) {
            for (Map.Entry<String, Map<String, TrinketInventory>> group : trinketComponent.getInventory().entrySet()) { for (Map.Entry<String, TrinketInventory> slotType : group.getValue().entrySet()) {

                TrinketInventory inventory = slotType.getValue();
                for (int i = 0; i < inventory.size(); i++) {

                    SlotReference slotReference = new SlotReference(inventory, i);
                    ItemStack currentStack = inventory.getStack(i);

                    ItemStack newStack = items.get(slotReference.getId());
                    if (newStack != null && !newStack.isEmpty()) {
                        if (currentStack.isEmpty()) {
                            inventory.setStack(i, newStack);
                        } else {
                            additionalItems.add(newStack);
                        }
                    }
                }
            }}

        } else {
            additionalItems.addAll(items.values());
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

    public static Map<String, ItemStack> inventoryFromNbt(NbtList nbtList, RegistryWrapper.WrapperLookup registries) {
        Map<String, ItemStack> map = new HashMap<>();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            String slotReference = nbtCompound.getString("Slot");
            ItemStack stack = ItemStack.fromNbt(registries, nbtCompound).orElse(ItemStack.EMPTY);

            if (!stack.isEmpty()) {
                map.put(slotReference, stack);
            }
        }

        return map;
    }
}
