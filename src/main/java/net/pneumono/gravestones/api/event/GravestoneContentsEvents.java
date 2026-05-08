package net.pneumono.gravestones.api.event;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.gravestones.GravestoneManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GravestoneContentsEvents {
    private static final List<Pair<Identifier, SkipItem>> SKIP_ITEM_LISTENERS = new ArrayList<>();
    private static final List<Pair<Identifier, InsertItem>> INSERT_ITEM_LISTENERS = new ArrayList<>();

    public static boolean invokeSkipItem(Player player, ItemStack stack, @Nullable Identifier slot) {
        for (Pair<Identifier, SkipItem> pair : SKIP_ITEM_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running SkipItem listener '{}'", id);
            SkipItem listener = pair.getSecond();
            if (listener.run(player, stack, slot)) {
                GravestoneManager.info("ItemStack '{}' skipped by listener '{}'", stack.getItem().toString(), id);
                return true;
            }
        }
        return false;
    }

    public static void invokeInsertItem(Player player, ItemStack stack, @Nullable Identifier slot) {
        for (Pair<Identifier, InsertItem> pair : INSERT_ITEM_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running InsertItem listener '{}'", id);
            InsertItem listener = pair.getSecond();
            listener.run(player, stack, slot);
        }
    }

    /**
     * Registers a {@link SkipItem} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerSkipItem(Identifier id, SkipItem listener) {
        SKIP_ITEM_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * Registers a {@link InsertItem} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerInsertItem(Identifier id, InsertItem listener) {
        INSERT_ITEM_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * SkipItem listeners are called for each item stack that might be inserted into the gravestone.
     *
     * <p>Returning {@code true} will "skip" the item stack, and it will be ignored by the gravestone.
     * Returning {@code false} will fall back to further processing.
     *
     * <p>If an item stack is skipped, the gravestone ignores it completely,
     * and it is treated as it would be if Gravestones was not installed.
     * Typically, this means it is dropped on the ground.
     *
     * <p>The slot identifier may be null if the item stack does not come from a specific slot
     * (e.g. a bundle-like inventory).
     * The slot identifier will often need to be converted to another format to be used,
     * make sure to check how the identifier is created in the relevant {@link GravestoneDataType}.
     *
     * <p>SkipItem listeners are ideal for preserving the on-death functionality of certain items,
     * since skipped items are still processed by vanilla or other mods after the gravestone is placed.
     * For example, items enchanted with Curse of Vanishing are skipped using a SkipItem listener,
     * which leaves them to be processed by vanilla death mechanics, which clear them from the inventory.
     *
     * <p>However, some items with on-death functionality should still be put into the gravestone.
     * In this situation, the on-death functionality will need to be triggered separately.
     * This should be done using an {@link InsertItem} listener.
     *
     * <p>SkipItem listeners should not be used to add on-death functionality,
     * as they may not run if a previous listener already skipped the item stack.
     * To add on-death functionality, use an {@link InsertItem} listener.
     */
    @FunctionalInterface
    public interface SkipItem {
        boolean run(Player player, ItemStack stack, @Nullable Identifier slot);
    }

    /**
     * InsertItem listeners are called for each item stack before it gets inserted into the gravestone.
     *
     * <p>InsertItem listeners are still called for item stacks that will be skipped by {@link SkipItem} listeners.
     *
     * <p>The slot identifier may be null if the item stack does not come from a specific slot
     * (e.g. a bundle-like inventory).
     * The slot identifier will often need to be converted to another format to be used,
     * make sure to check how the identifier is created in the relevant {@link GravestoneDataType}.
     *
     * <p>InsertItem listeners are ideal for items with on-death functionality that should still be inserted into gravestones.
     * For example, a version of a Totem of Undying that can be used multiple times.
     *
     * <p>For items with existing on-death functionality that should not be inserted into gravestones,
     * the {@code gravestones:skips_gravestones} tags or a {@link SkipItem} listener should be used.
     */
    @FunctionalInterface
    public interface InsertItem {
        void run(Player player, ItemStack stack, @Nullable Identifier slot);
    }
}
