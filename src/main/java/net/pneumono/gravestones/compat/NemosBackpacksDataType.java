package net.pneumono.gravestones.compat;

//? if nemos_backpacks {
import com.mojang.serialization.DynamicOps;
import com.nemonotfound.nemos.backpacks.NemosBackpacks;
import com.nemonotfound.nemos.backpacks.helper.BackpackGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.multiversion.VersionUtil;

public class NemosBackpacksDataType extends GravestoneDataType {
    private static final String KEY = "backpack";

    @Override
    public void writeData(CompoundTag tag, DynamicOps<Tag> ops, Player player) throws Exception {
        if (player.getInventory() instanceof BackpackGetter backpackGetter) {
            ItemStack stack = backpackGetter.nemosBackpacks$getBackpack().copy();
            GravestonesApi.onInsertItem(player, stack, VersionUtil.createId("nemos_backpacks", "backpack"));
            if (stack.isEmpty() || GravestonesApi.shouldSkipItem(player, stack)) return;

            VersionUtil.put(ops, tag, KEY, ItemStack.CODEC, stack);
            backpackGetter.nemosBackpacks$getBackpack().setCount(0);
        }
    }

    @Override
    public void onBreak(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, int decay) throws Exception {
        ItemStack stack = VersionUtil.get(ops, tag, KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (stack != ItemStack.EMPTY) {
            dropStack(level, pos, stack);
        }
    }

    @Override
    public void onCollect(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, Player player, int decay) throws Exception {
        ItemStack stack = VersionUtil.get(ops, tag, KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (stack != ItemStack.EMPTY) {
            if (player.getInventory().getItem(NemosBackpacks.BACKPACK_SLOT).isEmpty()) {
                player.getInventory().setItem(NemosBackpacks.BACKPACK_SLOT, stack);
            } else {
                dropStack(player, stack);
            }
        }
    }
}
//?}