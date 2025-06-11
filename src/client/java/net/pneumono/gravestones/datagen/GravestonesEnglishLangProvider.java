package net.pneumono.gravestones.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.gravestones.content.GravestonesRegistry;

import java.util.concurrent.CompletableFuture;

public class GravestonesEnglishLangProvider extends FabricLanguageProvider {
    public GravestonesEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder builder) {
        builder.add(GravestonesRegistry.GRAVESTONE, "Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_CHIPPED, "Chipped Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_DAMAGED, "Damaged Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_TECHNICAL, "Gravestone");

        builder.add(GravestonesRegistry.GRAVESTONE.asItem(), "Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_CHIPPED.asItem(), "Chipped Gravestone");
        builder.add(GravestonesRegistry.GRAVESTONE_DAMAGED.asItem(), "Damaged Gravestone");

        builder.add("gravestones.cannot_open_wrong_player", "This gravestone can only be interacted with by its owner, %s!");
        builder.add("gravestones.cannot_open_no_owner", "This gravestone has no owner!");
        builder.add("gravestones.player_collected_grave", "%s has found their grave!");
        builder.add("gravestones.player_collected_grave_at_coords", "%1$s has found their grave at %2$s!");
        builder.add("gravestones.grave_spawned", "%1$s's grave is at %2$s");
        builder.add("gravestones.edit_text", "Edit Gravestone Message");
        builder.add("gravestones.position", "(%1$s,%2$s,%3$s) in %4$s");

        builder.add(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, "Gravestone Skeleton");

        builder.add("stat.gravestones.gravestones_collected", "Gravestones Collected");

        builder.add("gravestones.subtitles.block.gravestone.waxed_interact_fail", "Gravestone thumps");

        builder.add("tag.item.gravestones.skips_gravestones", "Skips Gravestone Processing");
        builder.add("tag.enchantment.gravestones.skips_gravestones", "Skips Gravestone Processing");

        builder.add("commands.gravestones.getdata.gravestone.all_data", "Gravestone has a spawnDate of %1$s and a graveOwner of %2$s (%3$s)");
        builder.add("commands.gravestones.getdata.gravestone.no_grave_owner", "Gravestone has a spawnDate of %s and no graveOwner!");
        builder.add("commands.gravestones.getdata.gravestone.no_gravestone", "No gravestone at that position!");
        builder.add("commands.gravestones.getdata.gravestone.contents_data", "Gravestone has the following contents data: %s");
        builder.add("commands.gravestones.getdata.player.grave_data", "%1$s has graves at the following locations: %2$s, %3$s, %4$s");
        builder.add("commands.gravestones.getdata.player.cannot_read", "Could not read gravestones data file!");
        builder.add("commands.gravestones.getdata.player.cannot_find", "Could not find gravestones data file!");
        builder.add("commands.gravestones.deaths.view", "Inventory: %s");
        builder.add("commands.gravestones.deaths.recover", "Recovered inventory from death");
        builder.add("commands.gravestones.getuuid", "%1$s has UUID %2$s");

        builder.add("configs.gravestones.screen_title", "Gravestones Configs");
        builder.add("configs.gravestones.aesthetic_gravestones", "Aesthetic Gravestones");
        builder.add("configs.gravestones.aesthetic_gravestones.tooltip", "Whether or not aesthetic gravestones can be crafted for building purposes");
        builder.add("configs.gravestones.decay_with_time", "Time Decay");
        builder.add("configs.gravestones.decay_with_time.tooltip", "Whether or not gravestones are damaged over time");
        builder.add("configs.gravestones.decay_with_deaths", "Death Decay");
        builder.add("configs.gravestones.decay_with_deaths.tooltip", "Whether or not gravestones are damaged from further deaths");
        builder.add("configs.gravestones.decay_time", "Time to Decay");
        builder.add("configs.gravestones.decay_time.tooltip", "The amount of time it takes for gravestones to be damaged by time (if enabled)");
        builder.add("configs.gravestones.decay_time_type", "Decay Time Measurement");
        builder.add("configs.gravestones.decay_time_type.tooltip", "Whether decay time is based on real-world time (Real Time), or time the server is active (Ticks). Gravestone decay will occur regardless of whether chunks are loaded");
        builder.add("configs.gravestones.decay_time_type.ticks", "Ticks");
        builder.add("configs.gravestones.decay_time_type.real_time", "Real Time");
        builder.add("configs.gravestones.store_experience", "Store Experience");
        builder.add("configs.gravestones.store_experience.tooltip", "Whether or not graves store experience. If not, experience is dropped on the ground on death");
        builder.add("configs.gravestones.experience_cap", "Experience Cap");
        builder.add("configs.gravestones.experience_cap.tooltip", "Whether or not graves have a limit to how much experience they can store. This limit is the same as with vanilla deaths (100 points)");
        builder.add("configs.gravestones.experience_kept", "Experience Kept");
        builder.add("configs.gravestones.experience_kept.all", "All (100%)");
        builder.add("configs.gravestones.experience_kept.three_quarters", "3/4 (75%)");
        builder.add("configs.gravestones.experience_kept.two_thirds", "2/3 (66%)");
        builder.add("configs.gravestones.experience_kept.half", "1/2 (50%)");
        builder.add("configs.gravestones.experience_kept.one_third", "1/3 (33%)");
        builder.add("configs.gravestones.experience_kept.one_quarter", "1/4 (25%)");
        builder.add("configs.gravestones.experience_kept.vanilla", "Vanilla");
        builder.add("configs.gravestones.experience_kept.tooltip", "How experience kept on death is calculated. Either no experience is lost, a fraction is kept, or it uses the vanilla calculation (7 * current level)");
        builder.add("configs.gravestones.experience_decay", "Experience Decay");
        builder.add("configs.gravestones.experience_decay.tooltip", "Whether or not grave decay affects the experience stored in the grave (if enabled). If so, experience dropped is divided by the decay stage (stage 1 drops 100%, stage 2 drops 50%, and stage 3 drops 33%)");
        builder.add("configs.gravestones.gravestone_accessible_owner_only", "Owner-Only Access");
        builder.add("configs.gravestones.gravestone_accessible_owner_only.tooltip", "Whether or not players who aren't the owner are prevented from accessing a grave's items");
        builder.add("configs.gravestones.spawn_gravestone_skeletons", "Spawn Skeletons");
        builder.add("configs.gravestones.spawn_gravestone_skeletons.tooltip", "Whether or not gravestones spawn skeletons when their owner is near");
        builder.add("configs.gravestones.broadcast_collect_in_chat", "Broadcast Collection");
        builder.add("configs.gravestones.broadcast_collect_in_chat.tooltip", "Whether or not grave collection is broadcasted in chat");
        builder.add("configs.gravestones.broadcast_coordinates_in_chat", "Broadcast Coordinates");
        builder.add("configs.gravestones.broadcast_coordinates_in_chat.tooltip", "Whether or not the coordinates of placed graves are broadcasted in chat");
        builder.add("configs.gravestones.console_info", "Console Info");
        builder.add("configs.gravestones.console_info.tooltip", "Whether or not highly detailed information about graves is recorded in console. This is mostly just for debugging!");
        builder.add("configs.gravestones.time_format", "Time Format");
        builder.add("configs.gravestones.time_format.ddmmyyyy", "DD/MM/YYYY");
        builder.add("configs.gravestones.time_format.mmddyyyy", "MM/DD/YYYY");
        builder.add("configs.gravestones.time_format.yyyymmdd", "YYYY/MM/DD");
        builder.add("configs.gravestones.time_format.tooltip", "The format of the death time shown on gravestones");
        builder.add("configs.category.gravestones.decay", "Gravestone Decay");
        builder.add("configs.category.gravestones.experience", "Experience Storage");
        builder.add("configs.category.gravestones.multiplayer", "Multiplayer");
    }
}
