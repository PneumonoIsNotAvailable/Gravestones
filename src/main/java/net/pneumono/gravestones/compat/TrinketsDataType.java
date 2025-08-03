package net.pneumono.gravestones.compat;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.trinkets.api.*;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrinketsDataType extends GravestoneDataType {
    public static final Codec<Pair<SlotReferencePrimitive, ItemStack>> SLOT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SlotReferencePrimitive.CODEC.fieldOf("slot").forGetter(Pair::getFirst),
            ItemStack.CODEC.fieldOf("stack").forGetter(Pair::getSecond)
    ).apply(builder, Pair<SlotReferencePrimitive, ItemStack>::new));

    @Override
    public void writeData(NbtCompound view, PlayerEntity player) {
        TrinketComponent component = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (component == null) return;

        List<Pair<SlotReferencePrimitive, ItemStack>> storedTrinkets = new ArrayList<>();
        component.forEach((reference, stack) -> {
            Gravestones.LOGGER.info("fuck");
            if (shouldSkipTrinket(player, reference, stack)) return;

            storedTrinkets.add(new Pair<>(new SlotReferencePrimitive(reference), stack));
            reference.inventory().removeStack(reference.index());
        });

        DataResult<NbtElement> result = SLOT_CODEC.listOf().encodeStart(NbtOps.INSTANCE, storedTrinkets);
        if (result.isSuccess()) {
            view.put("trinkets", result.getOrThrow());
        }
    }

    public boolean shouldSkipTrinket(PlayerEntity player, SlotReference reference, ItemStack stack) {
        boolean shouldSkipItem = GravestonesApi.shouldSkipItem(player, stack);

        TrinketEnums.DropRule dropRule = TrinketsApi.getTrinket(stack.getItem()).getDropRule(stack, reference, player);

        dropRule = TrinketDropCallback.EVENT.invoker().drop(dropRule, stack, reference, player);

        TrinketInventory inventory = reference.inventory();

        if (dropRule == TrinketEnums.DropRule.DEFAULT) {
            dropRule = inventory.getSlotType().getDropRule();
        }

        if (dropRule == TrinketEnums.DropRule.DEFAULT) {
            if (EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                dropRule = TrinketEnums.DropRule.DESTROY;
            } else {
                dropRule = TrinketEnums.DropRule.DROP;
            }
        }

        return stack.isEmpty() || dropRule != TrinketEnums.DropRule.DROP || shouldSkipItem;
    }

    @Override
    public void onBreak(NbtCompound view, World world, BlockPos pos, int decay) {
        deserialize(view).stream()
                .map(Pair::getSecond)
                .forEach(stack -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }

    @Override
    public void onCollect(NbtCompound view, World world, BlockPos pos, PlayerEntity player, int decay) {
        TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(player).orElse(null);
        Map<String, Map<String, TrinketInventory>> trinketInventories;
        if (trinketComponent == null) {
            trinketInventories = null;
        } else {
            trinketInventories = trinketComponent.getInventory();
        }

        deserialize(view).stream()
                .filter(pair -> {
                    if (trinketInventories == null) return true;

                    SlotReferencePrimitive reference = pair.getFirst();
                    TrinketInventory inventory = trinketInventories.get(reference.groupName()).get(reference.slotName());

                    if (inventory.getStack(reference.index()).isEmpty()) {
                        inventory.setStack(reference.index(), pair.getSecond());
                        return false;
                    }

                    return true;
                })
                .forEach(pair -> {
                    ItemStack stack = pair.getSecond();
                    if (!player.giveItemStack(stack)) {
                        ItemEntity itemEntity = player.dropItem(stack, false);
                        if (itemEntity != null) {
                            itemEntity.resetPickupDelay();
                            itemEntity.setOwner(player.getUuid());
                        }
                    }
                });
    }

    private List<Pair<SlotReferencePrimitive, ItemStack>> deserialize(NbtCompound view) {
        DataResult<Pair<List<Pair<SlotReferencePrimitive, ItemStack>>, NbtElement>> result = SLOT_CODEC.listOf().decode(NbtOps.INSTANCE, view.getList("trinkets", NbtElement.COMPOUND_TYPE));
        if (result.isSuccess()) {
            return result.getOrThrow().getFirst();
        } else {
            return new ArrayList<>();
        }
    }

    public record SlotReferencePrimitive(String groupName, String slotName, int index) {
        public static final Codec<SlotReferencePrimitive> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("group").forGetter(SlotReferencePrimitive::groupName),
                Codec.STRING.fieldOf("slot").forGetter(SlotReferencePrimitive::slotName),
                Codec.INT.fieldOf("index").forGetter(SlotReferencePrimitive::index)
        ).apply(builder, SlotReferencePrimitive::new));

        public SlotReferencePrimitive(SlotReference reference) {
            this(
                    reference.inventory().getSlotType().getGroup(),
                    reference.inventory().getSlotType().getName(),
                    reference.index()
            );
        }
    }
}
