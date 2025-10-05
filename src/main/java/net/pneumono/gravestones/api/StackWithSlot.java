package net.pneumono.gravestones.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

/**
 * Similar to vanilla's {@code StackWithSlot}
 *
 * <p>Exists for backwards compatibility, as {@code StackWithSlot} only exists in 1.21.6+
 */
public record StackWithSlot(int slot, ItemStack stack) {
    public static final Codec<StackWithSlot> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.INT.fieldOf("slot").orElse(0).forGetter(StackWithSlot::slot),
            ItemStack.CODEC.fieldOf("item").forGetter(StackWithSlot::stack)
    ).apply(instance, StackWithSlot::new));

    public boolean isValidSlot(int slots) {
        return this.slot >= 0 && this.slot < slots;
    }

    public int slot() {
        return this.slot;
    }

    public ItemStack stack() {
        return this.stack;
    }
}
