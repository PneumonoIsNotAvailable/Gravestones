package net.pneumono.gravestones.compat;

//? if backpacked {
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.BackpackHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.List;

public class BackpackedDataType extends GravestoneDataType {
    public static final String KEY = "backpacks";

    @Override
    public void writeData(NbtCompound nbt, DynamicOps<NbtElement> ops, PlayerEntity player) throws Exception {
        List<SavedBackpack> savedBackpacks = new ArrayList<>();

        DefaultedList<ItemStack> backpacks = BackpackHelper.removeAllBackpacks(player);
        for (int index = 0; index < backpacks.size(); index++) {
            ItemStack backpack = backpacks.get(index);
            if (!backpack.isEmpty()) {
                savedBackpacks.add(new SavedBackpack(index, backpack));
            }
        }

        VersionUtil.put(ops, nbt, KEY, SavedBackpack.CODEC.listOf(), savedBackpacks);
    }

    @Override
    public void onBreak(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, int decay) throws Exception {
        List<SavedBackpack> savedBackpacks = VersionUtil.get(ops, nbt, KEY, SavedBackpack.CODEC.listOf()).orElse(new ArrayList<>());
        savedBackpacks.forEach(backpack -> dropStack(world, pos, backpack.stack()));
    }

    @Override
    public void onCollect(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, PlayerEntity player, int decay) throws Exception {
        List<SavedBackpack> savedBackpacks = VersionUtil.get(ops, nbt, KEY, SavedBackpack.CODEC.listOf()).orElse(new ArrayList<>());

        List<ItemStack> remaining = new ArrayList<>();
        for (SavedBackpack savedBackpack : savedBackpacks) {
            if (!BackpackHelper.getBackpackStack(player, savedBackpack.index()).isEmpty() || !BackpackHelper.setBackpackStack(player, savedBackpack.stack(), savedBackpack.index())) {
                remaining.add(savedBackpack.stack());
            }
        }

        remaining.forEach(stack -> dropStack(player, stack));
    }

    public record SavedBackpack(int index, ItemStack stack) {
        public static final Codec<SavedBackpack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("index").forGetter(SavedBackpack::index),
                ItemStack.CODEC.fieldOf("stack").forGetter(SavedBackpack::stack)
        ).apply(instance, SavedBackpack::new));
    }
}
//?}