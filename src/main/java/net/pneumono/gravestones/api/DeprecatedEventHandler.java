package net.pneumono.gravestones.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("removal")
public class DeprecatedEventHandler {
    public static void gravestonePlacedCallback(ServerLevel level, Player player, GlobalPos deathPos, GlobalPos gravePos) {
        GravestonePlacedCallback.EVENT.invoker().afterGravestonePlace(level, player, deathPos, gravePos);
    }

    public static GlobalPos redirectGravestonePositionCallback(ServerLevel level, Player player, GlobalPos deathPos) {
        return RedirectGravestonePositionCallback.EVENT.invoker().redirectPosition(level, player, deathPos);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean positionValidationCallback(Level level, BlockState state, BlockPos pos) {
        return PositionValidationCallback.EVENT.invoker().isPositionValid(level, state, pos);
    }

    public static boolean cancelGravestonePlacementCallback(ServerLevel level, Player player, GlobalPos deathPos) {
        return CancelGravestonePlacementCallback.EVENT.invoker().shouldCancel(level, player, deathPos);
    }

    public static boolean insertGravestoneItemCallback(Player player, ItemStack itemStack) {
        return InsertGravestoneItemCallback.EVENT.invoker().insertItem(player, itemStack);
    }

    public static void onInsertItemCallback(Player player, ItemStack itemStack, @Nullable Identifier slot) {
        OnInsertItemCallback.EVENT.invoker().insertItem(player, itemStack, slot);
    }

    public static boolean skipItemCallback(Player player, ItemStack itemStack, @Nullable Identifier slot) {
        return SkipItemCallback.EVENT.invoker().insertItem(player, itemStack, slot);
    }

    public static void gravestoneCollectedCallback(ServerLevel level, Player player, BlockPos pos) {
        GravestoneCollectedCallback.EVENT.invoker().afterGravestoneCollect(level, player, pos);
    }
}
