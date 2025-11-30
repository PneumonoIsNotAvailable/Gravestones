package net.pneumono.gravestones.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for handling all inventory mods
 */
public class InventoryModManager extends GravestoneDataType {
    public static final InventoryModManager INSTANCE = new InventoryModManager();
    private final List<InventoryModRecord> inventoryModList = new ArrayList<>();

    private InventoryModManager() {}

    /**
     * Registers an inventory mod to the manager
     * @param inventoryMod
     * @param modId
     */
    public void registerInventoryMod(InventoryMod inventoryMod, Identifier modId) {
        inventoryModList.add(new InventoryModRecord(inventoryMod, modId));
    }

    public void init() {
        GravestonesApi.registerDataType(Gravestones.id("inventory_mods"), this);

        // Register a fallback adapter for gravestones that didn't use this class
        for (InventoryModRecord i : inventoryModList) {
            if (i.modId != null) {
                GravestonesApi.registerDataType(i.modId, new FallbackAdapter(i.inventory));
            }
        }
    }

    @Override
    public void writeData(NbtCompound view, PlayerEntity player) {
        Gravestones.LOGGER.info("InventoryModManager: Saving data...");
        NbtCompound modData = new NbtCompound();
        for (InventoryModRecord i : inventoryModList) {
            i.inventory.writeData(modData, player);
        }
        view.put("items", modData);
    }

    @Override
    public void onBreak(NbtCompound view, World world, BlockPos pos, int decay) {
        if (view.contains("items")) {
            NbtCompound modData = view.getCompound("items");
            for (InventoryModRecord inventoryModRecord : inventoryModList) {
                inventoryModRecord.inventory.onBreak(modData, world, pos, decay);
            }
        }
    }

    @Override
    public void onCollect(NbtCompound view, World world, BlockPos pos, PlayerEntity player, int decay) {
        if (view.contains("items")) {
            NbtCompound modData = view.getCompound("items");
            for (InventoryModRecord inventoryModRecord : inventoryModList) {
                inventoryModRecord.inventory.onCollect(modData, world, pos, player, decay);
            }
        }
    }

    private record InventoryModRecord(InventoryMod inventory, Identifier modId) {}

    private static class FallbackAdapter extends GravestoneDataType {
        private final InventoryMod inventoryMod;

        public FallbackAdapter(InventoryMod inventoryMod) {
            this.inventoryMod = inventoryMod;
        }

        @Override
        public void writeData(NbtCompound view, PlayerEntity player) {
            // Fallback adapter does not write data
        }

        @Override
        public void onBreak(NbtCompound view, World world, BlockPos pos, int decay) {
            inventoryMod.onBreak(view, world, pos, decay);
        }

        @Override
        public void onCollect(NbtCompound view, World world, BlockPos pos, PlayerEntity player, int decay) {
            inventoryMod.onCollect(view, world, pos, player, decay);
        }
    }
}