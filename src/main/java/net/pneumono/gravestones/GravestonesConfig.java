package net.pneumono.gravestones;

import net.pneumono.gravestones.gravestones.enums.DecayTimeType;
import net.pneumono.gravestones.gravestones.enums.ExperienceKeptCalculation;
import net.pneumono.gravestones.gravestones.enums.TimeFormat;
import net.pneumono.pneumonocore.config_api.ConfigApi;
import net.pneumono.pneumonocore.config_api.configurations.*;
import net.pneumono.pneumonocore.config_api.enums.LoadType;
import net.pneumono.pneumonocore.config_api.enums.TimeUnit;

public class GravestonesConfig {
    public static final BooleanConfiguration DECAY_WITH_DEATHS = register("decay_with_deaths", new BooleanConfiguration(
            true, new ConfigSettings().category("decay").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration DECAY_WITH_TIME = register("decay_with_time", new BooleanConfiguration(
            true, new ConfigSettings().category("decay").loadType(LoadType.INSTANT)
    ));
    public static final TimeConfiguration DECAY_TIME = register("decay_time", new TimeConfiguration(
            8L * TimeUnit.HOURS.getDivision(), new ConfigSettings().category("decay").loadType(LoadType.INSTANT)
    ));
    public static final EnumConfiguration<DecayTimeType> DECAY_TIME_TYPE = register("decay_time_type", new EnumConfiguration<>(
            DecayTimeType.TICKS, new ConfigSettings().category("decay").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration AESTHETIC_DECAY = register("aesthetic_decay", new BooleanConfiguration(
            false, new ConfigSettings().category("decay").loadType(LoadType.INSTANT)
    ));

    public static final BooleanConfiguration STORE_EXPERIENCE = register("store_experience", new BooleanConfiguration(
            true, new ConfigSettings().category("experience").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration EXPERIENCE_CAP = register("experience_cap", new BooleanConfiguration(
            true, new ConfigSettings().category("experience").loadType(LoadType.INSTANT)
    ));
    public static final EnumConfiguration<ExperienceKeptCalculation> EXPERIENCE_KEPT = register("experience_kept", new EnumConfiguration<>(
            ExperienceKeptCalculation.VANILLA, new ConfigSettings().category("experience").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration EXPERIENCE_DECAY = register("experience_decay", new BooleanConfiguration(
            false, new ConfigSettings().category("experience").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration DROP_EXPERIENCE = register("drop_experience", new BooleanConfiguration(
            false, new ConfigSettings().category("experience").loadType(LoadType.INSTANT)
    ));

    public static final BooleanConfiguration GRAVESTONE_ACCESSIBLE_OWNER_ONLY = register("gravestone_accessible_owner_only", new BooleanConfiguration(
            true, new ConfigSettings().category("multiplayer").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration BROADCAST_COLLECT_IN_CHAT = register("broadcast_collect_in_chat", new BooleanConfiguration(
            false, new ConfigSettings().category("multiplayer").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration BROADCAST_COORDINATES_IN_CHAT = register("broadcast_coordinates_in_chat", new BooleanConfiguration(
            false, new ConfigSettings().category("multiplayer").loadType(LoadType.INSTANT)
    ));

    public static final BooleanConfiguration AESTHETIC_GRAVESTONES = register("aesthetic_gravestones", new BooleanConfiguration(
            true, new ConfigSettings().loadType(LoadType.RESTART)
    ));
    public static final BooleanConfiguration SPAWN_GRAVESTONE_SKELETONS = register("spawn_gravestone_skeletons", new BooleanConfiguration(
            false, new ConfigSettings().loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration SHOW_HEADS = register("show_heads", new BooleanConfiguration(
            true, new ConfigSettings().clientSide().category("clientside").loadType(LoadType.INSTANT)
    ));
    public static final EnumConfiguration<TimeFormat> TIME_FORMAT = register("time_format", new EnumConfiguration<>(
            TimeFormat.MMDDYYYY, new ConfigSettings().clientSide().category("clientside").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration CONSOLE_INFO = register("console_info", new BooleanConfiguration(
            false, new ConfigSettings().clientSide().category("clientside").loadType(LoadType.INSTANT)
    ));

    public static <T extends AbstractConfiguration<?>> T register(String name, T config) {
        return ConfigApi.register(Gravestones.id(name), config);
    }

    public static void registerGravestonesConfigs() {
        ConfigApi.finishRegistry(Gravestones.MOD_ID);
    }
}
