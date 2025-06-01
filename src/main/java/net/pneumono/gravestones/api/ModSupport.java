package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.pneumono.gravestones.content.entity.TechnicalGravestoneBlockEntity;

public abstract class ModSupport {
    /**
     * Called for each item in the player's inventory to see if the item should be put in the gravestone.<p>
     * If it returns {@code true}, the item is put in the gravestone as normal (assuming it passes checks from all other {@link ModSupport}s).<p>
     * If it returns {@code false}, the item is "skipped" by the gravestone, and is not put in the gravestone or cleared from the player's inventory. If nothing else is done other than returning {@code false}, this will result in the item being dropped on the ground, like in vanilla.<p>
     * This is ideal for supporting mods that would do other things on death, since it should just skip the gravestone process, then the other mod's code should run. However, if you return {@code false} and do not want this behaviour, make sure to set the stack's count to 0 using {@link ItemStack#setCount(int)} to make the item "disappear".<p>
     * This can also be used for doing other things with items on death (for example, an item that decrements by 1 on death) by simply changing whatever is needed about the item stack, and then returning {@code true}.
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @return Whether the item should be put in the gravestone.
     */
    public boolean shouldPutItemInGravestone(PlayerEntity player, ItemStack stack) {
        return true;
    }
}
