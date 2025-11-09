package net.pneumono.gravestones.compat;

//? if trinkets {
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.trinkets.api.*;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//? if >=1.21.1 {
import net.minecraft.component.EnchantmentEffectComponentTypes;
//?}

public class TrinketsDataType extends GravestoneDataType {
    private static final String KEY = "trinkets";

    @Override
    public void writeData(NbtCompound nbt, DynamicOps<NbtElement> ops, PlayerEntity player) throws Exception {
        TrinketComponent component = TrinketsApi.getTrinketComponent(player).orElse(null);
        if (component == null) return;

        List<TrinketsSlot> storedTrinkets = new ArrayList<>();
        component.forEach((reference, stack) -> {
            if (shouldSkipTrinket(player, reference, stack)) return;

            storedTrinkets.add(new TrinketsSlot(reference, stack));
            reference.inventory().removeStack(reference.index());
        });

        VersionUtil.put(ops, nbt, KEY, TrinketsSlot.CODEC.listOf(), storedTrinkets);
    }

    @Override
    public void onBreak(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, int decay) {
        List<TrinketsSlot> list = VersionUtil.get(ops, nbt, KEY, TrinketsSlot.CODEC.listOf()).orElseThrow();

        for (TrinketsSlot slot : list) {
            dropStack(world, pos, slot.stack());
        }
    }

    @Override
    public void onCollect(NbtCompound nbt, DynamicOps<NbtElement> ops, World world, BlockPos pos, PlayerEntity player, int decay) {
        List<TrinketsSlot> list = VersionUtil.get(ops, nbt, KEY, TrinketsSlot.CODEC.listOf()).orElseThrow();

        TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(player).orElse(null);
        Map<String, Map<String, TrinketInventory>> trinketInventories;
        if (trinketComponent == null) {
            trinketInventories = null;
        } else {
            trinketInventories = trinketComponent.getInventory();
        }

        List<TrinketsSlot> remainingSlots = new ArrayList<>();
        if (trinketInventories == null) {
            remainingSlots.addAll(list);
        } else {
            for (TrinketsSlot slot : list) {
                TrinketInventory inventory = trinketInventories.get(slot.groupName()).get(slot.slotName());

                if (inventory.getStack(slot.index()).isEmpty()) {
                    inventory.setStack(slot.index(), slot.stack());
                    continue;
                }

                remainingSlots.add(slot);
            }
        }

        for (TrinketsSlot slot : remainingSlots) {
            ItemStack stack = slot.stack();
            dropStack(player, stack);
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
            boolean vanishing =
            //? if >=1.21.1 {
            EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP);
            //?} else {
            /*EnchantmentHelper.hasVanishingCurse(stack);
            *///?}
            if (vanishing) {
                dropRule = TrinketEnums.DropRule.DESTROY;
            } else {
                dropRule = TrinketEnums.DropRule.DROP;
            }
        }

        return stack.isEmpty() || dropRule != TrinketEnums.DropRule.DROP || shouldSkipItem;
    }

    public record TrinketsSlot(String groupName, String slotName, int index, ItemStack stack) {
        public static final Codec<TrinketsSlot> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("group").forGetter(TrinketsSlot::groupName),
                Codec.STRING.fieldOf("slot").forGetter(TrinketsSlot::slotName),
                Codec.INT.fieldOf("index").forGetter(TrinketsSlot::index),
                ItemStack.CODEC.fieldOf("stack").forGetter(TrinketsSlot::stack)
        ).apply(builder, TrinketsSlot::new));

        public TrinketsSlot(SlotReference reference, ItemStack stack) {
            this(
                    reference.inventory().getSlotType().getGroup(),
                    reference.inventory().getSlotType().getName(),
                    reference.index(),
                    stack
            );
        }
    }
}
//?}