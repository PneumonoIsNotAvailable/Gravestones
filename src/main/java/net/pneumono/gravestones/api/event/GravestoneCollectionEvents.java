package net.pneumono.gravestones.api.event;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.pneumono.gravestones.api.GravestoneDataType;
import net.pneumono.gravestones.block.TechnicalGravestoneBlockEntity;
import net.pneumono.gravestones.gravestones.GravestoneManager;

import java.util.ArrayList;
import java.util.List;

public class GravestoneCollectionEvents {
    private static final List<Pair<Identifier, CancelCollect>> CANCEL_COLLECT_LISTENERS = new ArrayList<>();
    private static final List<Pair<Identifier, BeforeCollect>> BEFORE_COLLECT_LISTENERS = new ArrayList<>();
    private static final List<Pair<Identifier, AfterCollect>> AFTER_COLLECT_LISTENERS = new ArrayList<>();

    public static Component runCancelCollect(
            MinecraftServer server, Player player, GlobalPos pos, TechnicalGravestoneBlockEntity gravestone
    ) {
        for (Pair<Identifier, CancelCollect> pair : CANCEL_COLLECT_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running CancelCollect listener '{}'", id);
            CancelCollect listener = pair.getSecond();
            Component component = listener.run(server, player, pos, gravestone);
            if (component != null) {
                GravestoneManager.info("Gravestone collection canceled by listener '{}'", id);
                return component;
            }
        }
        return null;
    }

    public static void runBeforeCollect(
            MinecraftServer server, Player player, GlobalPos pos, TechnicalGravestoneBlockEntity gravestone
    ) {
        for (Pair<Identifier, BeforeCollect> pair : BEFORE_COLLECT_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running BeforeCollect listener '{}'", id);
            BeforeCollect listener = pair.getSecond();
            listener.run(server, player, pos, gravestone);
        }
    }

    public static void runAfterCollect(MinecraftServer server, Player player, GlobalPos pos) {
        for (Pair<Identifier, AfterCollect> pair : AFTER_COLLECT_LISTENERS) {
            Identifier id = pair.getFirst();
            GravestoneManager.info("Running AfterCollect listener '{}'", id);
            AfterCollect listener = pair.getSecond();
            listener.run(server, player, pos);
        }
    }

    /**
     * Registers a {@link CancelCollect} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerCancelCollect(Identifier id, CancelCollect listener) {
        CANCEL_COLLECT_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * Registers a {@link BeforeCollect} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerBeforeCollect(Identifier id, BeforeCollect listener) {
        BEFORE_COLLECT_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * Registers an {@link AfterCollect} listener.
     *
     * <p>The {@code id} is for logging purposes, so should clearly describe what the listener is for,
     * and use the id of the mod that registered it for the namespace.
     */
    public static void registerAfterCollect(Identifier id, AfterCollect listener) {
        AFTER_COLLECT_LISTENERS.add(new Pair<>(id, listener));
    }

    /**
     * CancelCollect listeners are run at the start of the gravestone collection process.
     *
     * <p>Returning a {@link Component} will cancel gravestone creation, as well as further processing.
     * The Component will be displayed to the client to explain the reason collection was canceled.
     * For example, "This gravestone cannot be collected while a boss is nearby!".
     * Returning {@code null} will fall back to further processing.
     *
     * <p>{@link GlobalPos} has a {@code dimension} field,
     * which can be used to get the level in which the grave is being collected using {@link MinecraftServer#getLevel(ResourceKey)}.
     */
    @FunctionalInterface
    public interface CancelCollect {
        Component run(MinecraftServer server, Player player, GlobalPos pos, TechnicalGravestoneBlockEntity gravestone);
    }

    /**
     * BeforeCollect listeners are run at the start of the gravestone collection process,
     * after {@link CancelCollect} listeners.
     *
     * <p>They will not run if a CancelCollect listener has canceled gravestone collection.
     *
     * <p>{@link GlobalPos} has a {@code dimension} field,
     * which can be used to get the level in which the grave is being collected using {@link MinecraftServer#getLevel(ResourceKey)}.
     *
     * <p>Should not be used for extracting data from gravestones.
     * To do this, use a {@link GravestoneDataType}.
     */
    @FunctionalInterface
    public interface BeforeCollect {
        void run(MinecraftServer server, Player player, GlobalPos pos, TechnicalGravestoneBlockEntity gravestone);
    }

    /**
     * AfterCollect listeners are run as the last step of the gravestone collection process.
     *
     * <p>At this point, the contents have been returned to the player, and the gravestone block and block entity have been destroyed.
     *
     * <p>{@link GlobalPos} has a {@code dimension} field,
     * which can be used to get the level in which the grave is being collected using {@link MinecraftServer#getLevel(ResourceKey)}.
     *
     * <p>Should not be used for extracting data from gravestones.
     * To do this, use a {@link GravestoneDataType}.
     */
    @FunctionalInterface
    public interface AfterCollect {
        void run(MinecraftServer server, Player player, GlobalPos pos);
    }
}
