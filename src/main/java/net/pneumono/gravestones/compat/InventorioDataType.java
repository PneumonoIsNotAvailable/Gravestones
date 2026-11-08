package net.pneumono.gravestones.compat;

//? if inventorio {
/*import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import de.rubixdev.inventorio.api.InventorioAPI;
import de.rubixdev.inventorio.player.PlayerInventoryAddon;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.api.StackWithSlot;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InventorioDataType extends GravestoneDataType {
    private static final String DEEP_POCKETS_KEY = "deep_pockets";
    private static final String UTILITY_BELT_KEY = "utility_belt";
    private static final String TOOL_BELT_KEY = "tool_belt";

    @Override
    public void writeData(CompoundTag tag, DynamicOps<Tag> ops, Player player) throws Exception {
        PlayerInventoryAddon addon = InventorioAPI.getInventoryAddon(player);
        if (addon == null) return;

        serializeAddonPart(player, addon.deepPockets, tag, DEEP_POCKETS_KEY, ops);
        serializeAddonPart(player, addon.utilityBelt, tag, UTILITY_BELT_KEY, ops);
        serializeAddonPart(player, addon.toolBelt, tag, TOOL_BELT_KEY, ops);
    }

    private void serializeAddonPart(Player player, List<ItemStack> inventory, CompoundTag tag, String key, DynamicOps<Tag> ops) {
        ListTag list = new ListTag();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            Identifier slotIdentifier = VersionUtil.createId("inventorio", key + "_" + i);
            GravestonesApi.onInsertItem(player, stack, slotIdentifier);
            if (!GravestonesApi.shouldSkipItem(player, stack, slotIdentifier) && !stack.isEmpty()) {
                ItemStack stackToSave = stack.copyAndClear();
                DataResult<Tag> result = StackWithSlot.CODEC.encodeStart(ops, new StackWithSlot(i, stackToSave));
                if (result.result().isPresent()) {
                    list.add(result.result().get());
                } else {
                    Gravestones.LOGGER.error("Failed to serialize item {} for gravestone: {}", stackToSave, result.error().get().message());
                    player.drop(
                            stackToSave, true,
                            //? if >=26.3 {
                            Prediction.PREDICTED
                             //?} else {
                            /^false
                            ^///?}
                    );
                }
            }
        }

        tag.put(key, list);
    }

    @Override
    public void onBreak(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, int decay) throws Exception {
        Consumer<List<StackWithSlot>> stacksConsumer = stacks -> stacks.forEach(
                stack -> dropStack(level, pos, stack.stack())
        );
        deserializeAddonPart(stacksConsumer, tag, DEEP_POCKETS_KEY, ops);
        deserializeAddonPart(stacksConsumer, tag, UTILITY_BELT_KEY, ops);
        deserializeAddonPart(stacksConsumer, tag, TOOL_BELT_KEY, ops);
    }

    @Override
    public void onCollect(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, Player player, int decay) throws Exception {
        PlayerInventoryAddon addon = InventorioAPI.getInventoryAddon(player);
        if (addon == null) {
            onBreak(tag, ops, level, pos, decay);
            return;
        }

        List<ItemStack> remainingStacks = new ArrayList<>();

        BiConsumer<List<StackWithSlot>, List<ItemStack>> biConsumer = (stacks, inventory) -> {
            for (StackWithSlot stackWithSlot : stacks) {
                int slot = stackWithSlot.slot();
                if (stackWithSlot.isValidSlot(inventory.size())) {

                    ItemStack stack = stackWithSlot.stack();
                    if (stack.isEmpty()) continue;

                    if (inventory.get(slot).isEmpty()) {
                        inventory.set(slot, stack);
                        continue;
                    }
                }
                remainingStacks.add(stackWithSlot.stack());
            }
        };

        deserializeAddonPart(stacks -> biConsumer.accept(stacks, addon.deepPockets), tag, DEEP_POCKETS_KEY, ops);
        deserializeAddonPart(stacks -> biConsumer.accept(stacks, addon.utilityBelt), tag, UTILITY_BELT_KEY, ops);
        deserializeAddonPart(stacks -> biConsumer.accept(stacks, addon.toolBelt), tag, TOOL_BELT_KEY, ops);

        dropStacks(player, remainingStacks);
    }

    private void deserializeAddonPart(Consumer<List<StackWithSlot>> stacksConsumer, CompoundTag tag, String key, DynamicOps<Tag> ops) {
        ListTag list = VersionUtil.getCompoundListOrEmpty(tag, key);

        List<StackWithSlot> stacks = new ArrayList<>();
        for (Tag element : list) {
            StackWithSlot.CODEC.decode(ops, element).resultOrPartial(Gravestones.LOGGER::error).ifPresent(pair -> stacks.add(pair.getFirst()));
        }
        stacksConsumer.accept(stacks);
    }
}
*///?}