package net.pneumono.gravestones.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class GravestonesApi {
    private static final Map<Identifier, GravestoneDataType> DATA_TYPES = new HashMap<>();
    private static final List<BiPredicate<PlayerEntity, ItemStack>> ITEM_SKIP_PREDICATES = new ArrayList<>();

    /**
     * Registers a type of data that gravestones save, and how gravestones should handle that data.
     */
    public static void registerDataType(Identifier identifier, GravestoneDataType dataType) {
        DATA_TYPES.put(identifier, dataType);
    }

    /**
     * Registers a predicate that returns {@code true} if an item stack should be skipped by gravestone inventory processing, and handled by vanilla item dropping, or any additional code added by other mods.<p>
     * Each item in the player's inventory is tested to see if the item should be put in the gravestone.<p>
     * If {@code true} is returned, the item is "skipped" by the gravestone, and is not put in the gravestone or cleared from the player's inventory. If nothing else is done other than returning {@code true}, this will result in the item being dropped on the ground, like in vanilla.<p>
     * If {@code false} is returned, the item is put in the gravestone as normal (assuming it passes tests from all other predicates).<p>
     * This is ideal for supporting mods that do other things to items on death, since returning {@code true} skips Gravestones' item handling, and then the other mod's code should run.<p>
     * This can also be used for supporting items that are modified on death but should still be inserted into gravestones (for example, an item that decrements by 1 on death) by simply modifying the item stack, and then returning {@code false}.
     */
    public static void registerItemSkipPredicate(BiPredicate<PlayerEntity, ItemStack> predicate) {
        ITEM_SKIP_PREDICATES.add(predicate);
    }

    public static NbtCompound getDataToInsert(PlayerEntity player) {
        NbtCompound contents = new NbtCompound();

        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Gravestones.LOGGER)) {
            ErrorReporter reporter = logging.makeChild(player.getErrorReporterContext());

            for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
                NbtWriteView view = NbtWriteView.create(reporter, player.getRegistryManager());
                entry.getValue().writeData(view, player);

                contents.put(
                        entry.getKey().toString(),
                        view.getNbt()
                );
            }
        }

        return contents;
    }

    public static void onBreak(World world, BlockPos pos, int decay, TechnicalGravestoneBlockEntity entity) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Gravestones.LOGGER)) {
            ErrorReporter reporter = logging.makeChild(entity.getReporterContext());
            onBreak(reporter, world.getRegistryManager(), world, pos, decay, entity.getContents());
        }
    }

    public static void onBreak(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, World world, BlockPos pos, int decay, NbtCompound contents) {
        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            String id = entry.getKey().toString();
            ReadView view = NbtReadView.create(reporter, registries, contents);

            entry.getValue().onBreak(
                    view.getReadView(id),
                    world,
                    pos,
                    decay
            );
        }
    }

    public static void onCollect(World world, BlockPos pos, PlayerEntity player, int decay, NbtCompound contents) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Gravestones.LOGGER)) {
            ErrorReporter reporter = logging.makeChild(player.getErrorReporterContext());
            onCollect(reporter, world.getRegistryManager(), world, pos, player, decay, contents);
        }
    }

    public static void onCollect(ErrorReporter reporter, RegistryWrapper.WrapperLookup registries, World world, BlockPos pos, PlayerEntity player, int decay, NbtCompound contents) {
        for (Map.Entry<Identifier, GravestoneDataType> entry : DATA_TYPES.entrySet()) {
            String id = entry.getKey().toString();
            ReadView view = NbtReadView.create(reporter, registries, contents);

            entry.getValue().onCollect(
                    view.getReadView(id),
                    world,
                    pos,
                    player,
                    decay
            );
        }
    }

    /**
     * Checks against all registered item skip predicates to see whether an item stack should be skipped by gravestone processing.<p>
     *
     * @param player The player who has died.
     * @param stack The stack being checked.
     * @return Whether the item should be inserted.
     * @see GravestonesApi#registerItemSkipPredicate(BiPredicate)
     */
    public static boolean shouldSkipItem(PlayerEntity player, ItemStack stack) {
        for (BiPredicate<PlayerEntity, ItemStack> predicate : ITEM_SKIP_PREDICATES) {
            if (predicate.test(player, stack)) {
                return true;
            }
        }
        return false;
    }
}
