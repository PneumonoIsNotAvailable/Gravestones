package net.pneumono.gravestones.content.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class TrinketNbtHelper {
    public static NbtCompound serializeSlotData(List<Pair<SlotReferencePrimitive, ItemStack>> slotData) {
        NbtCompound compoundTag = new NbtCompound();
        NbtList listTag = new NbtList();

        for (Pair<SlotReferencePrimitive, ItemStack> pair : slotData) {
            NbtCompound slotTag = new NbtCompound();
            slotTag.putString("slotName", pair.getLeft().slotName());
            slotTag.putString("groupName", pair.getLeft().groupName());

            NbtCompound itemStackTag = new NbtCompound();
            pair.getRight().writeNbt(itemStackTag);
            slotTag.put("itemStack", itemStackTag);

            listTag.add(slotTag);
        }

        compoundTag.put("slotData", listTag);
        return compoundTag;
    }

    public static List<Pair<SlotReferencePrimitive, ItemStack>> deserializeSlotData(NbtCompound compoundTag) {
        List<Pair<SlotReferencePrimitive, ItemStack>> slotData = new ArrayList<>();
        NbtList listTag = compoundTag.getList("slotData", 10); // 10 is the tag type for CompoundTag

        for (int i = 0; i < listTag.size(); i++) {
            NbtCompound slotTag = listTag.getCompound(i);

            String slotName = slotTag.getString("slotName");
            String groupName = slotTag.getString("groupName");
            SlotReferencePrimitive slotReference = new SlotReferencePrimitive(groupName, slotName);

            NbtCompound itemStackTag = slotTag.getCompound("itemStack");
            ItemStack itemStack = ItemStack.fromNbt(itemStackTag);

            slotData.add(new Pair<>(slotReference, itemStack));
        }

        return slotData;
    }
}
