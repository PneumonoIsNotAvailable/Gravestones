package net.pneumono.gravestones.compat;

//? if accessories {
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.multiversion.VersionUtil;
import net.pneumono.pneumonocore.util.MultiVersionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//? if >=1.21.5 {
import io.wispforest.accessories.api.core.Accessory;
import io.wispforest.accessories.api.core.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotPredicateRegistry;
import io.wispforest.accessories.impl.core.ExpandedContainer;
//?} else if >=1.21.3 {
/*import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotPredicateRegistry;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
*///?} else {
/*import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
*///?}

//? if >=1.21.4 {
import io.wispforest.accessories.Accessories;
//?}

public class AccessoriesDataType extends GravestoneDataType {
    private static final String KEY = "accessories";

    //? if >=1.21.4 {
    @SuppressWarnings("UnstableApiUsage")
    //?}
    @Override
    public void writeData(NbtCompound nbt, DynamicOps<NbtElement> ops, PlayerEntity player) throws Exception {
        MinecraftServer server = MultiVersionUtil.getWorld(player).getServer();
        if (server == null /*? if >=1.21.4 {*/|| server.getGameRules().getBoolean(Accessories.RULE_KEEP_ACCESSORY_INVENTORY)/*?}*/) {
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
            /*ExpandedSimpleContainer accessories = container.getAccessories();
            *///?}
            for (int index = 0; index < accessories.size(); ++index) {
                ItemStack stack = accessories.getStack(index);
                GravestonesApi.onInsertItem(player, stack, Objects.requireNonNull(Identifier.tryParse(name)).withSuffixedPath("/" + index + "/" + "normal"));
                if (!GravestonesApi.shouldSkipItem(player, stack) && !stack.isEmpty()) {
                    accessories.removeStack(index);
                    list.add(new SlotReferencePrimitive(stack, name, index, false));
                }
            }

            //? if >=1.21.5 {
            ExpandedContainer cosmetics = container.getCosmeticAccessories();
            //?} else {
            /*ExpandedSimpleContainer cosmetics = container.getCosmeticAccessories();
            *///?}
            for (int index = 0; index < cosmetics.size(); ++index) {
                ItemStack stack = cosmetics.getStack(index);
                GravestonesApi.onInsertItem(player, stack, Objects.requireNonNull(Identifier.tryParse(name)).withSuffixedPath("/" + index + "/" + "cosmetic"));
                if (!GravestonesApi.shouldSkipItem(player, stack) && !stack.isEmpty()) {
                    cosmetics.removeStack(index);
                    list.add(new SlotReferencePrimitive(stack, name, index, true));
                }
            }
        }

        VersionUtil.put(ops, nbt, KEY, SlotReferencePrimitive.CODEC.listOf(), list);
    }

    @Override
    public void onBreak(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, int decay) {
        List<SlotReferencePrimitive> list = VersionUtil.get(ops, nbt, KEY, SlotReferencePrimitive.CODEC.listOf()).orElse(null);
        if (list == null || list.isEmpty()) return;

        dropStacks(world, pos, list.stream().map(SlotReferencePrimitive::stack).toList());
    }

    @Override
    public void onCollect(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, PlayerEntity player, int decay) {
        List<SlotReferencePrimitive> list = VersionUtil.get(ops, nbt, KEY, SlotReferencePrimitive.CODEC.listOf()).orElse(null);
        if (list == null || list.isEmpty()) return;

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) {
            warn("Player {} does not have an AccessoriesCapability. Any accessories will be dropped on the ground", player.getName().getString());
            dropStacks(world, pos, list.stream().map(SlotReferencePrimitive::stack).toList());
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
            //? if >=1.21.3 {
            SlotPredicateRegistry.canInsertIntoSlot(newStack, reference);
            //?} else {
            /*AccessoriesAPI.canInsertIntoSlot(newStack, reference);
            *///?}
            if (!canInsert) {
                remaining.add(newStack);
                continue;
            }

            //? if >=1.21.5 {
            ExpandedContainer accessories = primitive.cosmetic ? container.getCosmeticAccessories() : container.getAccessories();
            //?} else {
            /*ExpandedSimpleContainer accessories = primitive.cosmetic ? container.getCosmeticAccessories() : container.getAccessories();
            *///?}

            ItemStack oldStack = accessories.getStack(index);

            boolean canUnequipOldStack =
            //? if >=1.21.3 {
            AccessoryRegistry.canUnequip(oldStack, reference);
            //?} else {
            /*AccessoriesAPI.canUnequip(oldStack, reference);
            *///?}
            boolean canInsertNewStack =
            //? if >=1.21.3 {
            SlotPredicateRegistry.canInsertIntoSlot(newStack, reference);
            //?} else {
            /*AccessoriesAPI.canInsertIntoSlot(newStack, reference);
            *///?}

            if (oldStack.isEmpty() && canUnequipOldStack && canInsertNewStack
            ) {
                Accessory accessory =
                //? if >=1.21.3 {
                AccessoryRegistry.getAccessoryOrDefault(oldStack);
                //?} else {
                /*AccessoriesAPI.getOrDefaultAccessory(oldStack);
                *///?}
                ItemStack splitStack = newStack.split(accessory.maxStackSize(newStack));
                accessories.setStack(index, splitStack);
                if (!newStack.isEmpty()) {
                    remaining.add(newStack);
                }
            }
        }

        dropStacks(world, pos, remaining);
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
//?}
