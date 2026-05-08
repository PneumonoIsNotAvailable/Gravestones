package net.pneumono.gravestones.api.event;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.gravestones.GravestoneManager;

import java.util.ArrayList;
import java.util.List;

public class GravestonePlacementEvents {
    private static final List<Pair<Identifier, CancelPlace>> CANCEL_PLACE_LISTENERS = new ArrayList<>();
    private static final List<Pair<Identifier, RedirectPosition>> REDIRECT_POSITION_LISTENERS = new ArrayList<>();
    private static final List<Pair<Identifier, ValidatePosition>> VALIDATE_POSITION_LISTENERS = new ArrayList<>();
    private static final List<Pair<Identifier, BeforePlace>> BEFORE_PLACE_LISTENERS = new ArrayList<>();
    private static final List<Pair<Identifier, AfterPlace>> AFTER_PLACE_LISTENERS = new ArrayList<>();

    public static boolean runCancelPlace(MinecraftServer server, Player player, GlobalPos deathPos) {
        for (Pair<Identifier, CancelPlace> pair : CANCEL_PLACE_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running CancelPlace listener '{}'", id);
            CancelPlace listener = pair.getSecond();
            if (listener.run(server, player, deathPos)) {
                GravestoneManager.info("Gravestone placement canceled by listener '{}'", id);
                return true;
            }
        }
        return false;
    }

    public static void runBeforePlace(MinecraftServer server, Player player, GlobalPos deathPos) {
        for (Pair<Identifier, BeforePlace> pair : BEFORE_PLACE_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running BeforePlace listener '{}'", id);
            BeforePlace listener = pair.getSecond();
            listener.run(server, player, deathPos);
        }
    }

    public static void runAfterPlace(MinecraftServer server, Player player, GlobalPos deathPos, GlobalPos placementPos) {
        for (Pair<Identifier, AfterPlace> pair : AFTER_PLACE_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running AfterPlace listener '{}'", id);
            AfterPlace listener = pair.getSecond();
            listener.run(server, player, deathPos, placementPos);
        }
    }

    public static GlobalPos runRedirectPosition(MinecraftServer server, Player player, GlobalPos pos) {
        for (Pair<Identifier, RedirectPosition> pair : REDIRECT_POSITION_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running RedirectPosition listener '{}'", id);
            RedirectPosition listener = pair.getSecond();
            GlobalPos newPos = listener.run(server, player, pos);
            if (newPos != null && !newPos.equals(pos)) {
                GravestoneManager.info("Gravestone placement redirected by listener '{}'", id);
                return newPos;
            }
        }
        return pos;
    }

    public static boolean runValidatePosition(ServerLevel level, BlockState state, BlockPos pos) {
        for (Pair<Identifier, ValidatePosition> pair : VALIDATE_POSITION_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running ValidatePosition listener '{}'", id);
            ValidatePosition listener = pair.getSecond();
            if (!listener.run(level, state, pos)) {
                GravestoneManager.info("Position ({}) deemed invalid by listener '{}'", pos.toShortString(), id);
                return false;
            }
        }
        return true;
    }

    /**
     * Registers a {@link CancelPlace} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerCancelPlace(Identifier id, CancelPlace listener) {
        CANCEL_PLACE_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * Registers a {@link BeforePlace} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerBeforePlace(Identifier id, BeforePlace listener) {
        BEFORE_PLACE_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * Registers an {@link AfterPlace} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerAfterPlace(Identifier id, AfterPlace listener) {
        AFTER_PLACE_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * Registers a {@link RedirectPosition} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerRedirectPosition(Identifier id, RedirectPosition listener) {
        REDIRECT_POSITION_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * Registers a {@link ValidatePosition} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerValidatePosition(Identifier id, ValidatePosition listener) {
        VALIDATE_POSITION_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * CancelPlace listeners are run at the start of the gravestone creation process.
     *
     * <p>Returning {@code true} will cancel gravestone creation, as well as further processing.
     * Returning {@code false} will fall back to further processing.
     *
     * <p>{@link GlobalPos} has a {@code dimension} field,
     * which can be used to get the level in which the player died using {@link MinecraftServer#getLevel(ResourceKey)}.
     *
     * <p>CancelPlace listeners are ideal for situations in which gravestones should not be placed at all.
     * In many situations, it may instead be better to move the gravestone to another position (or dimension).
     * To do this, use a {@link RedirectPosition} listener.
     *
     * <p>Should not be used for checking if a position is valid.
     * To do this, use a {@link ValidatePosition} listener.
     */
    @FunctionalInterface
    public interface CancelPlace {
        boolean run(MinecraftServer server, Player player, GlobalPos deathPos);
    }

    /**
     * BeforePlace listeners are run at the start of the gravestone creation process,
     * after {@link CancelPlace CancelPlace} listeners.
     *
     * <p>They will not run if a CancelPlace listener has canceled gravestone creation.
     *
     * <p>{@link GlobalPos} has a {@code dimension} field,
     * which can be used to get the level in which the player died using {@link MinecraftServer#getLevel(ResourceKey)}.
     *
     * <p>Should not be used for inserting data into gravestones.
     * To do this, use a {@link GravestoneDataType}.
     *
     * <p>Cannot be used to cancel gravestone creation.
     * To do this, use a {@link CancelPlace} listener.
     */
    @FunctionalInterface
    public interface BeforePlace {
        void run(MinecraftServer server, Player player, GlobalPos deathPos);
    }

    /**
     * AfterPlace listeners are run as the last step of the gravestone creation process.
     *
     * <p>{@link GlobalPos} has a {@code dimension} field,
     * which can be used to get the level in which the player died, or the gravestone was placed,
     * using {@link MinecraftServer#getLevel(ResourceKey)}.
     *
     * <p>Should not be used for inserting data into gravestones.
     * To do this, use a {@link GravestoneDataType}.
     */
    @FunctionalInterface
    public interface AfterPlace {
        void run(MinecraftServer server, Player player, GlobalPos deathPos, GlobalPos placementPos);
    }

    /**
     * RedirectPosition listeners are run each time a "valid position" is found during the gravestone creation process.
     * Typically, this is once for the death position, and then again each time a RedirectPosition listener moves it.
     * This ensures that the final position meets the standards of all RedirectPosition listeners.
     *
     * <p>Returning a {@link GlobalPos} that is not the given position will redirect the gravestone to that position,
     * and trigger all RedirectPosition listeners again.
     * Returning {@code null}, or the given position, will fall back to further processing.
     *
     * <p>Because a successful redirection triggers all RedirectPosition listeners again,
     * you must make sure your listener does not end up infinitely looping.
     *
     * <p>RedirectPosition listeners are ideal for mods that do not want gravestones placed in particular locations.
     * For example, a roguelike dungeon mod would need to place the gravestone somewhere at the entrance of the dungeon,
     * so that items aren't lost when the dungeon is reset.
     *
     * <p>{@link GlobalPos} has a {@code dimension} field,
     * which can be used to get the level in which the player died using {@link MinecraftServer#getLevel(ResourceKey)},
     * and changed if necessary to move the gravestone to another dimension.
     *
     * <p>There is one builtin RedirectPosition listener,
     * which moves the gravestone to the nearest valid position if the current position is occupied.
     *
     * <p>Cannot not be used to cancel gravestone creation.
     * To do this, use a {@link CancelPlace} listener.
     */
    @FunctionalInterface
    public interface RedirectPosition {
        GlobalPos run(MinecraftServer server, Player player, GlobalPos pos);
    }

    /**
     * ValidatePosition listeners are run for each block position to see if they are valid for gravestone placement.
     *
     * <p>Returning {@code false} will prevent the gravestone from being placed at the position,
     * and cancel further processing.
     * Returning {@code true} will fall back to further processing.
     *
     * <p>ValidatePosition listeners are ideal for preventing certain types of blocks being destroyed.
     * For example, an important treasure block.
     *
     * <p>A ValidatePosition listener already exists that invalidates blocks with a default destroy time of < 1,
     * or an explosion resistance of >=3600000.
     * If these criteria are already met, you do not need to register another ValidatePosition listener.
     *
     * <p>If you want to prevent gravestones being placed in a larger area,
     * you may want to use a {@link RedirectPosition} listener instead.
     *
     * <p>The {@code gravestones:gravestone_irreplaceable} block tag should be used instead of this where possible.
     */
    @FunctionalInterface
    public interface ValidatePosition {
        boolean run(ServerLevel level, BlockState state, BlockPos possiblePos);
    }
}
