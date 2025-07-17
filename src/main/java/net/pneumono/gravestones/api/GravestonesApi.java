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
import net.pneumono.gravestones.GravestonesConfig;
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
     * @deprecated Use {@link InsertGravestoneItemCallback#EVENT} instead.
     */
    @Deprecated
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
        if (contents.isEmpty()) return;

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
     * Checks all registered {@link InsertGravestoneItemCallback} listeners, and item skip predicates,
     * to see whether an item stack should be skipped by gravestone processing.
     *
     * <p>This should be called before checking anything else about the stack,
     * as listeners may change it (e.g. emptying it).
     *
     * @param player The player who has died.
     * @param stack The item stack being checked.
     * @return Whether the item should be skipped.
     * @see InsertGravestoneItemCallback#EVENT
     */
    public static boolean shouldSkipItem(PlayerEntity player, ItemStack stack) {
        if (InsertGravestoneItemCallback.EVENT.invoker().insertItem(player, stack)) {
            return true;
        }

        for (BiPredicate<PlayerEntity, ItemStack> predicate : ITEM_SKIP_PREDICATES) {
            if (predicate.test(player, stack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * If the Experience Decay config is enabled, applies experience decay to an amount of experience.
     * Otherwise, does nothing.
     *
     * @param experience The initial experience amount
     * @param decay The decay stage of the gravestone
     * @return The final (decayed) experience amount
     */
    public static int getDecayedExperience(int experience, int decay) {
        if (GravestonesConfig.EXPERIENCE_DECAY.getValue()) {
            return experience / (decay + 1);
        } else {
            return experience;
        }
    }
}
