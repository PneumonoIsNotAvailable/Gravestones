package net.pneumono.gravestones.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.gravestones.content.GravestonesRegistry;

import java.util.concurrent.CompletableFuture;

public class GravestonesLolcatLangProvider extends FabricLanguageProvider {
    public GravestonesLolcatLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "lol_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
        builder.add(GravestonesRegistry.GRAVESTONE, "Die box");
        builder.add(GravestonesRegistry.GRAVESTONE_CHIPPED, "Half brokn die box");
        builder.add(GravestonesRegistry.GRAVESTONE_DAMAGED, "Very brokn die box!!");
        builder.add(GravestonesRegistry.GRAVESTONE_TECHNICAL, "Die box");

        builder.add(GravestonesRegistry.GRAVESTONE.asItem(), "Die box");
        builder.add(GravestonesRegistry.GRAVESTONE_CHIPPED.asItem(), "Half brokn die box");
        builder.add(GravestonesRegistry.GRAVESTONE_DAMAGED.asItem(), "Very brokn die box!!");

        builder.add("gravestones.cannot_open_wrong_player", "Ownly %s kan open da die box!");
        builder.add("gravestones.cannot_open_no_owner", "Die box has no kitteh!");
        builder.add("gravestones.player_collected_grave", "%s got bak die box!!");
        builder.add("gravestones.player_collected_grave_at_coords", "%1$s got bak die box at %2$s!!");
        builder.add("gravestones.grave_spawned", "%1$s lefty die box att %2$s!");
        builder.add("gravestones.edit_text", "Chaenj die box mesag");
        builder.add("gravestones.position", "(%1$s,%2$s,%3$s) in %4$s");

        builder.add(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, "Die box Skeletun");

        builder.add("stat.gravestones.gravestones_collected", "Die boxz got bak");

        builder.add("gravestones.subtitles.block.gravestone.waxed_interact_fail", "Die box go thump");

        builder.add("tag.item.gravestones.skips_gravestones", "Not 4 die box!");
        builder.add("tag.enchantment.gravestones.skips_gravestones", "Not 4 die box!");

        builder.add("commands.gravestones.getdata.gravestone.all_data", "Die box waz made %1$s by %2$s (%3$s) dyin");
        builder.add("commands.gravestones.getdata.gravestone.no_grave_owner", "Die box waz made %s but no cat ownah!");
        builder.add("commands.gravestones.getdata.gravestone.no_gravestone", "No die box ther!!");
        builder.add("commands.gravestones.getdata.gravestone.contents_data", "Die box haz codey numbrz: %s");
        builder.add("commands.gravestones.getdata.player.grave_data", "%1$s haz die boxz arund: %2$s, %3$s, %4$s");
        builder.add("commands.gravestones.getdata.player.cannot_read", "Kitteh cant reed die box fiel!");
        builder.add("commands.gravestones.getdata.player.cannot_find", "Kitteh cant find die box fiel!");
        builder.add("commands.gravestones.deaths.view", "Itemz: %s");
        builder.add("commands.gravestones.deaths.recover", "Got stuffs bak");
        builder.add("commands.gravestones.getuuid", "%1$s haz cat ID %2$s");

        builder.add("configs.gravestones.screen_title", "Die box settinz");
        builder.add("configs.gravestones.aesthetic_gravestones", "Pretteh die boxz");
        builder.add("configs.gravestones.aesthetic_gravestones.tooltip", "If I can haz pretteh die boxz 4 dekrait littrbox");
        builder.add("configs.gravestones.decay_with_time", "Die box gets old");
        builder.add("configs.gravestones.decay_with_time.tooltip", "If die boxz die frm old");
        builder.add("configs.gravestones.decay_with_deaths", "Die box ouchy when u die");
        builder.add("configs.gravestones.decay_with_deaths.tooltip", "If die boxz ouchy more when u die agen");
        builder.add("configs.gravestones.decay_time", "Olding r8");
        builder.add("configs.gravestones.decay_time.tooltip", "How much long it takez 4 die box to old");
        builder.add("configs.gravestones.decay_time_type", "how keep trak?");
        builder.add("configs.gravestones.decay_time_type.tooltip", "If oldness mezurd by reel-life (bad), or by fleas and tiks (also bad). Die boxz still gets old when kittehs not near!");
        builder.add("configs.gravestones.decay_time_type.ticks", "Tiks");
        builder.add("configs.gravestones.decay_time_type.real_time", "Reel life");
        builder.add("configs.gravestones.store_experience", "Rember levlz");
        builder.add("configs.gravestones.store_experience.tooltip", "If die boxz has ur levlz in dem. If not ur expee wil go on floor!!");
        builder.add("configs.gravestones.experience_cap", "Kewl limit");
        builder.add("configs.gravestones.experience_cap.tooltip", "If die boxz only saiv some of ur levlz. Kewlnes limit is da same as norml dies (100 points)");
        builder.add("configs.gravestones.experience_kept", "Levl keeping amownt");
        builder.add("configs.gravestones.experience_kept.all", "ALL OF DEM (100%)");
        builder.add("configs.gravestones.experience_kept.three_quarters", "3/4 (75%)");
        builder.add("configs.gravestones.experience_kept.two_thirds", "2/3 (66%)");
        builder.add("configs.gravestones.experience_kept.half", "1/2 (50%)");
        builder.add("configs.gravestones.experience_kept.one_third", "1/3 (33%)");
        builder.add("configs.gravestones.experience_kept.one_quarter", "1/4 (25%)");
        builder.add("configs.gravestones.experience_kept.vanilla", "norml");
        builder.add("configs.gravestones.experience_kept.tooltip", "How ur levlz kept gets knowed. Kan use frakshuns or da normal numberz (7 * currnt lvelz)");
        builder.add("configs.gravestones.experience_decay", "Expee gets old 2!??");
        builder.add("configs.gravestones.experience_decay.tooltip", "If die box olding takes away som of ur levz. Normly dropz 100%, but wen ouchied dropz 50%, and wen almost dyin dropz 33%)");
        builder.add("configs.gravestones.gravestone_accessible_owner_only", "STEALINGG");
        builder.add("configs.gravestones.gravestone_accessible_owner_only.tooltip", "If othr kittehz kan steel ur stuffs!!");
        builder.add("configs.gravestones.spawn_gravestone_skeletons", "spuky skary skeletuns");
        builder.add("configs.gravestones.spawn_gravestone_skeletons.tooltip", "If die boxz make skeletun frends sometiems");
        builder.add("configs.gravestones.broadcast_collect_in_chat", "evry1 sees collekt");
        builder.add("configs.gravestones.broadcast_collect_in_chat.tooltip", "If gettin ur stuffs bak gets told 2 evry1");
        builder.add("configs.gravestones.broadcast_coordinates_in_chat", "evry1 sees die place");
        builder.add("configs.gravestones.broadcast_coordinates_in_chat.tooltip", "If dyin means evry1 sees wher u dieded");
        builder.add("configs.gravestones.console_info", "Clevr knowings");
        builder.add("configs.gravestones.console_info.tooltip", "Putz smart kitteh knowings in ur consol. FOR CLEVR COED CATS ONLY!!");
        builder.add("configs.gravestones.time_format", "Tiem way");
        builder.add("configs.gravestones.time_format.ddmmyyyy", "DD/MM/YYYY");
        builder.add("configs.gravestones.time_format.mmddyyyy", "MM/DD/YYYY");
        builder.add("configs.gravestones.time_format.yyyymmdd", "YYYY/MM/DD");
        builder.add("configs.gravestones.time_format.tooltip", "Wat way u tel da tiem");
        builder.add("configs.category.gravestones.decay", "Die box oldin");
        builder.add("configs.category.gravestones.experience", "Kewlness");
        builder.add("configs.category.gravestones.multiplayer", "Wiv othr catz");
    }
}
