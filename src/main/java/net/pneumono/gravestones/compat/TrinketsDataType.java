package net.pneumono.gravestones.compat;

//? if trinkets {
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.multiversion.VersionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//? if >=26.1 {
import eu.pb4.trinkets.api.*;
//?} else {
/*import dev.emi.trinkets.api.*;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
*///?}

//? if >=1.21 {
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
//?}

public class TrinketsDataType extends GravestoneDataType {
    private static final String KEY = "trinkets";

    @Override
    public void writeData(CompoundTag tag, DynamicOps<Tag> ops, Player player) throws Exception {
        //? if >=26.1 {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        //?} else {
        /*TrinketComponent attachment = TrinketsApi.getTrinketComponent(player).orElse(null);
        *///?}
        if (attachment == null) return;

        List<TrinketsSlot> storedTrinkets = new ArrayList<>();
        attachment.forEach((reference, stack) -> {
            GravestonesApi.onInsertItem(player, stack, getId(reference));
            if (shouldSkipTrinket(player, reference, stack)) return;

            storedTrinkets.add(new TrinketsSlot(reference, stack));
            reference.inventory().removeItemNoUpdate(reference.index());
        });

        VersionUtil.put(ops, tag, KEY, TrinketsSlot.CODEC.listOf(), storedTrinkets);
    }

    private Identifier getId(/*? if >=26.1 {*/TrinketSlotAccess/*?} else {*//*SlotReference*//*?}*/ reference) {
        SlotType slotType = reference.inventory()./*? if >=26.1 {*/slotType/*getSlotType*//*?}*/();
        //? if >=26.1 {
        return VersionUtil.createId("trinkets",
                slotType.group() + "/" + slotType.getId() + "/" + reference.index());
        //?} else {
        /*return VersionUtil.createId("trinkets",
                slotType.getGroup() + "/" + slotType.getName() + "/" + reference.index());
        *///?}
    }

    @Override
    public void onBreak(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, int decay) {
        List<TrinketsSlot> list = VersionUtil.get(ops, tag, KEY, TrinketsSlot.CODEC.listOf()).orElseThrow();

        for (TrinketsSlot slot : list) {
            dropStack(level, pos, slot.stack());
        }
    }

    @Override
    public void onCollect(CompoundTag tag, DynamicOps<Tag> ops, Level level, BlockPos pos, Player player, int decay) {
        List<TrinketsSlot> list = VersionUtil.get(ops, tag, KEY, TrinketsSlot.CODEC.listOf()).orElseThrow();

        //? if >=26.1 {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        Map<String, TrinketInventory> trinketInventories;
        if (attachment == null) {
            trinketInventories = null;
        } else {
            trinketInventories = attachment.getInventories();
        }
        //?} else {
        /*TrinketComponent attachment = TrinketsApi.getTrinketComponent(player).orElse(null);
        Map<String, Map<String, TrinketInventory>> trinketInventories;
        if (attachment == null) {
            trinketInventories = null;
        } else {
            trinketInventories = attachment.getInventory();
        }
        *///?}

        List<TrinketsSlot> remainingSlots = new ArrayList<>();
        if (trinketInventories == null) {
            remainingSlots.addAll(list);
        } else {
            for (TrinketsSlot slot : list) {
                //? if >=26.1 {
                TrinketInventory inventory = trinketInventories.get(slot.slotId());
                //?} else {
                /*TrinketInventory inventory = trinketInventories.get(slot.groupName()).get(slot.slotId());
                *///?}

                if (inventory.getItem(slot.index()).isEmpty()) {
                    inventory.setItem(slot.index(), slot.stack());
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

    public boolean shouldSkipTrinket(Player player, /*? if >=26.1 {*/TrinketSlotAccess/*?} else {*//*SlotReference*//*?}*/ slot, ItemStack stack) {
        boolean shouldSkipItem = GravestonesApi.shouldSkipItem(player, stack);

        //? if >=26.1 {
        TrinketDropRule dropRule = TrinketsApi.getDropRule(stack, slot, player, false);
        //?} else {
        /*TrinketEnums.DropRule dropRule = TrinketsApi.getTrinket(stack.getItem()).getDropRule(stack, slot, player);

        dropRule = TrinketDropCallback.EVENT.invoker().drop(dropRule, stack, slot, player);

        TrinketInventory inventory = slot.inventory();

        if (dropRule == TrinketEnums.DropRule.DEFAULT) {
            dropRule = inventory.getSlotType().getDropRule();
        }

        if (dropRule == TrinketEnums.DropRule.DEFAULT) {
            boolean vanishing =
            //? if >=1.21 {
            EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP);
            //?} else {
            /^EnchantmentHelper.hasVanishingCurse(stack);
            ^///?}
            if (vanishing) {
                dropRule = TrinketEnums.DropRule.DESTROY;
            } else {
                dropRule = TrinketEnums.DropRule.DROP;
            }
        }
        *///?}

        return stack.isEmpty() || dropRule != /*? if >=26.1 {*/TrinketDropRule/*?} else {*//*TrinketEnums.DropRule*//*?}*/.DROP || shouldSkipItem;
    }

    public record TrinketsSlot(String groupName, String slotId, int index, ItemStack stack) {
        public static final Codec<TrinketsSlot> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("group").forGetter(TrinketsSlot::groupName),
                Codec.STRING.fieldOf("slot").forGetter(TrinketsSlot::slotId),
                Codec.INT.fieldOf("index").forGetter(TrinketsSlot::index),
                ItemStack.CODEC.fieldOf("stack").forGetter(TrinketsSlot::stack)
        ).apply(builder, TrinketsSlot::new));

        public TrinketsSlot(/*? if >=26.1 {*/TrinketSlotAccess/*?} else {*//*SlotReference*//*?}*/ slot, ItemStack stack) {
            this(
                    slot.inventory()./*? if >=26.1 {*/slotType/*getSlotType*//*?}*/()./*? if >=26.1 {*/group/*getGroup*//*?}*/(),
                    slot.inventory()./*? if >=26.1 {*/slotType/*getSlotType*//*?}*/()./*? if >=26.1 {*/getId/*getName*//*?}*/(),
                    slot.index(),
                    stack
            );
        }
    }
}
//?}