package net.pneumono.gravestones.content;

import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerInventoryDataType extends GravestoneDataType {
    @Override
    public NbtElement getDataToInsert(PlayerEntity player) {
        NbtCompound nbt = new NbtCompound();

        PlayerInventory inventory = player.getInventory();
        nbt.put("inventory", inventory.writeNbt(new NbtList()));
        RegistryOps<NbtElement> registryOps = player.getRegistryManager().getOps(NbtOps.INSTANCE);
        nbt.put("equipment", EntityEquipment.CODEC, registryOps, createEntityEquipment(player));

        inventory.clear();

        return nbt;
    }

    // Scuffed
    private EntityEquipment createEntityEquipment(PlayerEntity player) {
        EntityEquipment equipment = new EntityEquipment();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, player.getEquippedStack(slot));
        }
        return equipment;
    }

    @Override
    public void onBreak(World world, BlockPos pos, int decay, NbtElement element) {
        if (element == null) return;
        Optional<NbtCompound> optional = element.asCompound();
        if (optional.isEmpty()) return;
        NbtCompound nbt = optional.get();

        Optional<NbtList> inventoryOptional = nbt.getList("inventory");
        if (inventoryOptional.isPresent()) {
            NbtList nbtList = inventoryOptional.get();

            for (int i = 0; i < nbtList.size(); i++) {
                NbtCompound nbtCompound = nbtList.getCompoundOrEmpty(i);
                ItemStack stack = ItemStack.fromNbt(world.getRegistryManager(), nbtCompound).orElse(ItemStack.EMPTY);
                if (!stack.isEmpty()) {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }
        }

        RegistryOps<NbtElement> registryOps = world.getRegistryManager().getOps(NbtOps.INSTANCE);

        EntityEquipment equipment = nbt.get("equipment", EntityEquipment.CODEC, registryOps).orElseGet(EntityEquipment::new);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.MAINHAND) continue;

            ItemStack stack = equipment.get(slot);
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    @Override
    public void onCollect(PlayerEntity player, int decay, NbtElement element) {
        if (element == null) return;
        Optional<NbtCompound> optional = element.asCompound();
        if (optional.isEmpty()) return;
        NbtCompound nbt = optional.get();

        DefaultedList<ItemStack> playerInventory = player.getInventory().getMainStacks();
        List<ItemStack> additionalItems = new ArrayList<>();

        Optional<NbtList> inventoryOptional = nbt.getList("inventory");
        if (inventoryOptional.isPresent()) {
            NbtList nbtList = inventoryOptional.get();

            for (int i = 0; i < nbtList.size(); i++) {
                NbtCompound nbtCompound = nbtList.getCompoundOrEmpty(i);
                int j = nbtCompound.getByte("Slot", (byte)0) & 255;
                ItemStack itemStack = ItemStack.fromNbt(player.getRegistryManager(), nbtCompound).orElse(ItemStack.EMPTY);
                if (!itemStack.isEmpty()) {
                    if (j < playerInventory.size() && playerInventory.get(j).isEmpty()) {
                        playerInventory.set(j, itemStack);
                    } else {
                        additionalItems.add(itemStack);
                    }
                }
            }
        }

        RegistryOps<NbtElement> registryOps = player.getRegistryManager().getOps(NbtOps.INSTANCE);

        EntityEquipment equipment = nbt.get("equipment", EntityEquipment.CODEC, registryOps).orElseGet(EntityEquipment::new);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.MAINHAND) continue;

            ItemStack stack = equipment.get(slot);
            if (player.getEquippedStack(slot).isEmpty()) {
                player.equipStack(slot, stack);
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
}
