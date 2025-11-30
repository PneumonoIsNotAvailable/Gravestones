package net.pneumono.gravestones.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// Interface like GravestoneDataType, but for inventory mods specifically
public interface InventoryMod {
    String getId();

    void writeData(NbtCompound view, PlayerEntity player);

    void onBreak(NbtCompound view, World world, BlockPos pos, int decay);

    void onCollect(NbtCompound view, World world, BlockPos pos, PlayerEntity player, int decay);
}
