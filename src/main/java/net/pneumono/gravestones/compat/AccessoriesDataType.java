package net.pneumono.gravestones.compat;

//? if accessories {
/*import com.mojang.serialization.Codec;
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
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

//? if >=1.21.5 {
import io.wispforest.accessories.api.core.Accessory;
import io.wispforest.accessories.api.core.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotPredicateRegistry;
import io.wispforest.accessories.impl.core.ExpandedContainer;
//?} else if >=1.21.3 {
/^import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotPredicateRegistry;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
^///?} else {
/^import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
^///?}

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
        MinecraftServer server = player.getServer();
        if (server != null /^? if >=1.21.4 {^/&& server.getGameRules().getBoolean(Accessories.RULE_KEEP_ACCESSORY_INVENTORY)/^?}^/) {
            return;
        }

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) {
            throw new IllegalStateException("Player {} does not have an AccessoriesCapability");
        }

        List<SlotReferencePrimitive> list = capability.getAllEquipped().stream()
                .filter(reference -> !GravestonesApi.shouldSkipItem(player, reference.stack()))
                .map(reference -> {
                    ItemStack stack = reference.stack();
                    reference.reference().setStack(ItemStack.EMPTY);
                    return new SlotReferencePrimitive(stack, reference.reference());
                })
                .toList();

        VersionUtil.put(ops, nbt, KEY, SlotReferencePrimitive.CODEC.listOf(), list);
    }

    @Override
    public void onBreak(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, int decay) {
        List<SlotReferencePrimitive> list = VersionUtil.get(ops, nbt, KEY, SlotReferencePrimitive.CODEC.listOf()).orElse(null);
        if (list == null || list.isEmpty()) return;

        dropStacks(world, pos, list.stream().map(SlotReferencePrimitive::stack));
    }

    @Override
    public void onCollect(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, PlayerEntity player, int decay) {
        List<SlotReferencePrimitive> list = VersionUtil.get(ops, nbt, KEY, SlotReferencePrimitive.CODEC.listOf()).orElse(null);
        if (list == null || list.isEmpty()) return;

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) {
            warn("Player {} does not have an AccessoriesCapability. Any accessories will be dropped on the ground", player.getName().getString());
            dropStacks(world, pos, list.stream().map(SlotReferencePrimitive::stack));
            return;
        }

        List<ItemStack> remaining = new ArrayList<>();

        for (SlotReferencePrimitive primitive : list) {
            ItemStack newStack = primitive.stack;
            if (newStack.isEmpty()) continue;
            int index = primitive.index;

            AccessoriesContainer container = capability.getContainers().get(primitive.slotName);
            if (container == null || container.getSize() <= 0) {
                remaining.add(newStack);
                continue;
            }

            boolean canInsert =
            //? if >=1.21.3 {
            SlotPredicateRegistry.canInsertIntoSlot(newStack, container.createReference(primitive.index));
            //?} else {
            /^AccessoriesAPI.canInsertIntoSlot(newStack, container.createReference(primitive.index));
            ^///?}
            if (!canInsert) {
                remaining.add(newStack);
                continue;
            }

            //? if >=1.21.5 {
            ExpandedContainer accessories = container.getAccessories();
            //?} else {
            /^ExpandedSimpleContainer accessories = container.getAccessories();
            ^///?}

            ItemStack oldStack = accessories.getStack(index);
            SlotReference slotReference = container.createReference(index);

            boolean canUnequipOldStack =
            //? if >=1.21.3 {
            AccessoryRegistry.canUnequip(oldStack, slotReference);
            //?} else {
            /^AccessoriesAPI.canUnequip(oldStack, slotReference);
            ^///?}
            boolean canInsertNewStack =
            //? if >=1.21.3 {
            SlotPredicateRegistry.canInsertIntoSlot(newStack, slotReference);
            //?} else {
            /^AccessoriesAPI.canInsertIntoSlot(newStack, slotReference);
            ^///?}

            if (oldStack.isEmpty() && canUnequipOldStack && canInsertNewStack
            ) {
                Accessory accessory =
                //? if >=1.21.3 {
                AccessoryRegistry.getAccessoryOrDefault(oldStack);
                //?} else {
                /^AccessoriesAPI.getOrDefaultAccessory(oldStack);
                ^///?}
                ItemStack splitStack = newStack.split(accessory.maxStackSize(newStack));

                slotReference.setStack(splitStack);
            }
        }

        dropStacks(world, pos, remaining.stream());
    }

    public void dropStacks(World world, BlockPos pos, Stream<ItemStack> stream) {
        stream.filter(stack -> !stack.isEmpty())
                .forEach(stack -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }

    //? if >=1.21.5 {
    public record SlotReferencePrimitive(ItemStack stack, String slotName, int index, List<Integer> innerIndices, boolean isNested) {
        public static final Codec<SlotReferencePrimitive> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("newStack").forGetter(SlotReferencePrimitive::stack),
                Codec.STRING.fieldOf("slot_name").forGetter(SlotReferencePrimitive::slotName),
                Codec.INT.fieldOf("index").forGetter(SlotReferencePrimitive::index),
                Codec.INT.listOf().fieldOf("inner_indices").forGetter(SlotReferencePrimitive::innerIndices),
                Codec.BOOL.fieldOf("nested").forGetter(SlotReferencePrimitive::isNested)
        ).apply(instance, SlotReferencePrimitive::new));

        public SlotReferencePrimitive(ItemStack stack, SlotReference reference) {
            this(
                    stack,
                    reference.slotName(),
                    reference.index(),
                    reference.innerIndices(),
                    reference.isNested()
            );
        }
    }
    //?} else {
    /^public record SlotReferencePrimitive(ItemStack stack, String slotName, int index) {
        public static final Codec<SlotReferencePrimitive> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("newStack").forGetter(SlotReferencePrimitive::stack),
                Codec.STRING.fieldOf("slot_name").forGetter(SlotReferencePrimitive::slotName),
                Codec.INT.fieldOf("index").forGetter(SlotReferencePrimitive::index)
        ).apply(instance, SlotReferencePrimitive::new));

        public SlotReferencePrimitive(ItemStack stack, SlotReference reference) {
            this(
                    stack,
                    reference.slotName(),
                    reference.slot()
            );
        }
    }
    ^///?}
}
*///?}
