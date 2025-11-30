package net.pneumono.gravestones.compat;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AccessoriesDataType implements InventoryMod {
    @Override
    public String getId() {
        return "accessories";
    }

    @Override
    public void writeData(NbtCompound view, PlayerEntity player) {
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

        DataResult<NbtElement> result = SlotReferencePrimitive.CODEC.listOf().encodeStart(player.getRegistryManager().getOps(NbtOps.INSTANCE), list);
        if (result.isSuccess()) {
            view.put("accessories", result.getOrThrow());
        }
    }

    @Override
    public void onBreak(NbtCompound view, World world, BlockPos pos, int decay) {
        List<SlotReferencePrimitive> list = deserialize(view, world.getRegistryManager());
        if (list == null || list.isEmpty()) return;

        dropStacks(world, pos, list.stream().map(SlotReferencePrimitive::stack));
    }

    @Override
    public void onCollect(NbtCompound view, World world, BlockPos pos, PlayerEntity player, int decay) {
        List<SlotReferencePrimitive> list = deserialize(view, player.getRegistryManager());
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

            SlotReference slotReference = container.createReference(index);
            if (!AccessoriesAPI.canInsertIntoSlot(newStack, slotReference)) {
                remaining.add(newStack);
                continue;
            }

            ExpandedSimpleContainer accessories = container.getAccessories();

            ItemStack oldStack = accessories.getStack(index);
            if (oldStack.isEmpty()
                    && AccessoriesAPI.canUnequip(oldStack, slotReference)
            ) {
                Accessory accessory = AccessoriesAPI.getOrDefaultAccessory(oldStack);
                ItemStack splitStack = newStack.split(accessory.maxStackSize(newStack));

                slotReference.setStack(splitStack);
            }
        }

        dropStacks(world, pos, remaining.stream());
    }

    public List<SlotReferencePrimitive> deserialize(NbtCompound view, DynamicRegistryManager registryManager) {
        DataResult<Pair<List<SlotReferencePrimitive>, NbtElement>> result = SlotReferencePrimitive.CODEC.listOf()
                .decode(registryManager.getOps(NbtOps.INSTANCE), view.get("accessories"));
        if (result.isSuccess()) {
            return result.getOrThrow().getFirst();
        } else {
            return new ArrayList<>();
        }
    }

    public void dropStacks(World world, BlockPos pos, Stream<ItemStack> stream) {
        stream.filter(stack -> !stack.isEmpty())
                .forEach(stack -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }

    public record SlotReferencePrimitive(ItemStack stack, String slotName, int index) {
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
}
