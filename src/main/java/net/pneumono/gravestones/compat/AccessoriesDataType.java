package net.pneumono.gravestones.compat;

//? if accessories {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//? if >=1.21.5 {
import io.wispforest.accessories.api.core.Accessory;
import io.wispforest.accessories.api.core.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotPredicateRegistry;
import io.wispforest.accessories.impl.core.ExpandedContainer;
//?} else if >=1.21.2 {
/^import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotPredicateRegistry;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
^///?} else {
/^import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
^///?}

//? if >=1.21.9 {
import io.wispforest.accessories.misc.AccessoriesGameRules;
//?} else if >=1.21.4 {
/^import io.wispforest.accessories.Accessories;
^///?}

//? if >=1.21.11 {
import net.minecraft.world.level.gamerules.GameRules;
//?} else >=1.21.4 {
/^import net.minecraft.world.level.GameRules;
^///?}

public class AccessoriesDataType extends GravestoneDataType {
    private static final String KEY = "accessories";

    //? if >=1.21.4 {
    @SuppressWarnings("UnstableApiUsage")
    //?}
    @Override
    public void writeData(CompoundTag tag, DynamicOps<Tag> ops, Player player) throws Exception {
        MinecraftServer server = player.level().getServer();
        //? if >=1.21.9 {
        GameRules.Key<GameRules.BooleanValue> gameRule = AccessoriesGameRules.RULE_KEEP_ACCESSORY_INVENTORY;
        //?} else if >=1.21.4 {
        /^GameRules.Key<GameRules.BooleanValue> gameRule = Accessories.RULE_KEEP_ACCESSORY_INVENTORY;
        ^///?}
        if (server == null /^? if >=1.21.4 {^/|| server.getGameRules().getBoolean(gameRule)/^?}^/) {
            return;
        }

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) {
            throw new IllegalStateException("Player {} does not have an AccessoriesCapability");
        }

        List<SlotReferencePrimitive> list = new ArrayList<>();
        for (Map.Entry<String, AccessoriesContainer> entry : capability.getContainers().entrySet()) {
            String name = entry.getKey();
            AccessoriesContainer container = entry.getValue();

            //? if >=1.21.5 {
            ExpandedContainer accessories = container.getAccessories();
            //?} else {
            /^ExpandedSimpleContainer accessories = container.getAccessories();
            ^///?}
            for (int index = 0; index < accessories.getContainerSize(); ++index) {
                ItemStack stack = accessories.getItem(index);
                GravestonesApi.onInsertItem(player, stack, Objects.requireNonNull(Identifier.tryParse(name)).withSuffix("/" + index + "/" + "normal"));
                if (!GravestonesApi.shouldSkipItem(player, stack) && !stack.isEmpty()) {
                    accessories.setItem(index, ItemStack.EMPTY);
                    list.add(new SlotReferencePrimitive(stack, name, index, false));
                }
            }

            //? if >=1.21.5 {
            ExpandedContainer cosmetics = container.getCosmeticAccessories();
            //?} else {
            /^ExpandedSimpleContainer cosmetics = container.getCosmeticAccessories();
            ^///?}
            for (int index = 0; index < cosmetics.getContainerSize(); ++index) {
                ItemStack stack = cosmetics.getItem(index);
                GravestonesApi.onInsertItem(player, stack, Objects.requireNonNull(Identifier.tryParse(name)).withSuffix("/" + index + "/" + "cosmetic"));
                if (!GravestonesApi.shouldSkipItem(player, stack) && !stack.isEmpty()) {
                    cosmetics.setItem(index, ItemStack.EMPTY);
                    list.add(new SlotReferencePrimitive(stack, name, index, true));
                }
            }
        }

        VersionUtil.put(ops, tag, KEY, SlotReferencePrimitive.CODEC.listOf(), list);
    }

    @Override
    public void onBreak(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, int decay) {
        List<SlotReferencePrimitive> list = VersionUtil.get(ops, tag, KEY, SlotReferencePrimitive.CODEC.listOf()).orElse(null);
        if (list == null || list.isEmpty()) return;

        dropStacks(level, pos, list.stream().map(SlotReferencePrimitive::stack).toList());
    }

    @Override
    public void onCollect(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, Player player, int decay) {
        List<SlotReferencePrimitive> list = VersionUtil.get(ops, tag, KEY, SlotReferencePrimitive.CODEC.listOf()).orElse(null);
        if (list == null || list.isEmpty()) return;

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) {
            warn("Player {} does not have an AccessoriesCapability. Any accessories will be dropped on the ground", player.getName().getString());
            dropStacks(level, pos, list.stream().map(SlotReferencePrimitive::stack).toList());
            return;
        }

        List<ItemStack> remaining = new ArrayList<>();
        for (SlotReferencePrimitive primitive : list) {
            ItemStack newStack = primitive.stack;
            if (newStack.isEmpty()) continue;
            int index = primitive.index;

            AccessoriesContainer container = capability.getContainers().get(primitive.slotName());
            if (container == null || container.getSize() <= 0) {
                remaining.add(newStack);
                continue;
            }

            SlotReference reference = SlotReference.of(player, primitive.slotName(), primitive.index());

            boolean canInsert =
            //? if >=1.21.2 {
            SlotPredicateRegistry.canInsertIntoSlot(newStack, reference);
            //?} else {
            /^AccessoriesAPI.canInsertIntoSlot(newStack, reference);
            ^///?}
            if (!canInsert) {
                remaining.add(newStack);
                continue;
            }

            //? if >=1.21.5 {
            ExpandedContainer accessories = primitive.cosmetic ? container.getCosmeticAccessories() : container.getAccessories();
            //?} else {
            /^ExpandedSimpleContainer accessories = primitive.cosmetic ? container.getCosmeticAccessories() : container.getAccessories();
            ^///?}

            ItemStack oldStack = accessories.getItem(index);

            boolean canUnequipOldStack =
            //? if >=1.21.2 {
            AccessoryRegistry.canUnequip(oldStack, reference);
            //?} else {
            /^AccessoriesAPI.canUnequip(oldStack, reference);
            ^///?}
            boolean canInsertNewStack =
            //? if >=1.21.2 {
            SlotPredicateRegistry.canInsertIntoSlot(newStack, reference);
            //?} else {
            /^AccessoriesAPI.canInsertIntoSlot(newStack, reference);
            ^///?}

            if (oldStack.isEmpty() && canUnequipOldStack && canInsertNewStack
            ) {
                Accessory accessory =
                //? if >=1.21.2 {
                AccessoryRegistry.getAccessoryOrDefault(oldStack);
                //?} else {
                /^AccessoriesAPI.getOrDefaultAccessory(oldStack);
                ^///?}
                ItemStack splitStack = newStack.split(accessory.maxStackSize(newStack));
                accessories.setItem(index, splitStack);
                if (!newStack.isEmpty()) {
                    remaining.add(newStack);
                }
            }
        }

        dropStacks(level, pos, remaining);
    }

    public record SlotReferencePrimitive(ItemStack stack, String slotName, int index, boolean cosmetic) {
        public static final Codec<SlotReferencePrimitive> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("stack").forGetter(SlotReferencePrimitive::stack),
                Codec.STRING.fieldOf("slot_name").forGetter(SlotReferencePrimitive::slotName),
                Codec.INT.fieldOf("index").forGetter(SlotReferencePrimitive::index),
                Codec.BOOL.fieldOf("cosmetic").forGetter(SlotReferencePrimitive::cosmetic)
        ).apply(instance, SlotReferencePrimitive::new));
    }
}
*///?}
