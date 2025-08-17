package net.pneumono.gravestones.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.core.Accessory;
import io.wispforest.accessories.api.core.AccessoryRegistry;
import io.wispforest.accessories.api.slot.SlotPredicateRegistry;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.impl.core.ExpandedContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AccessoriesDataType extends GravestoneDataType {
    @Override
    public void writeData(WriteView view, PlayerEntity player) {
        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) return;

        List<SlotReferencePrimitive> list = capability.getAllEquipped().stream()
                .filter(reference -> !GravestonesApi.shouldSkipItem(player, reference.stack()))
                .map(reference -> {
                    ItemStack stack = reference.stack();
                    reference.reference().setStack(ItemStack.EMPTY);
                    return new SlotReferencePrimitive(stack, reference.reference());
                })
                .toList();

        view.put(
                "accessories",
                SlotReferencePrimitive.CODEC.listOf(),
                list
        );
    }

    @Override
    public void onBreak(ReadView view, World world, BlockPos pos, int decay) {
        List<SlotReferencePrimitive> list = view.read("accessories", SlotReferencePrimitive.CODEC.listOf()).orElse(null);
        if (list == null || list.isEmpty()) return;

        dropStacks(world, pos, list.stream().map(SlotReferencePrimitive::stack));
    }

    @Override
    public void onCollect(ReadView view, World world, BlockPos pos, PlayerEntity player, int decay) {
        List<SlotReferencePrimitive> list = view.read("accessories", SlotReferencePrimitive.CODEC.listOf()).orElse(null);
        if (list == null || list.isEmpty()) return;

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) return;

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

            if (!SlotPredicateRegistry.canInsertIntoSlot(newStack, container.createReference(primitive.index))) {
                remaining.add(newStack);
                continue;
            }

            ExpandedContainer accessories = container.getAccessories();

            ItemStack oldStack = accessories.getStack(index);
            SlotReference slotReference = container.createReference(index);

            if (oldStack.isEmpty()
                    && AccessoryRegistry.canUnequip(oldStack, slotReference)
                    && SlotPredicateRegistry.canInsertIntoSlot(newStack, slotReference)
            ) {
                Accessory accessory = AccessoryRegistry.getAccessoryOrDefault(oldStack);
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
}
