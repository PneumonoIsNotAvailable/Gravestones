package net.pneumono.gravestones.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.BiConsumer;

public class TechnicalGravestoneBlock extends AbstractGravestoneBlock {
    public static final MapCodec<TechnicalGravestoneBlock> CODEC = TechnicalGravestoneBlock.createCodec(TechnicalGravestoneBlock::new);
    public static final IntProperty DAMAGE = IntProperty.of("damage", 0, 2);

    public TechnicalGravestoneBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DAMAGE);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player instanceof FakePlayer || !(world.getBlockEntity(pos) instanceof TechnicalGravestoneBlockEntity gravestone)) {
            return ActionResult.FAIL;
        }

        if (world.isClient()) return ActionResult.SUCCESS;

        createSoulParticles(world, pos);

        ProfileComponent graveOwner = gravestone.getGraveOwner();
        if (graveOwner == null) {
            player.sendMessage(Text.translatable("gravestones.cannot_open_no_owner"), true);
            return ActionResult.SUCCESS;
        }

        boolean isOwner = graveOwner.gameProfile().getId().equals(player.getGameProfile().getId());
        if (!isOwner && GravestonesConfig.GRAVESTONE_ACCESSIBLE_OWNER_ONLY.getValue()) {
            player.sendMessage(Text.translatable("gravestones.cannot_open_wrong_player", graveOwner.name().orElse("???")), true);
            return ActionResult.SUCCESS;
        }

        String uuid = "";
        if (GravestonesConfig.CONSOLE_INFO.getValue()) {
            uuid = " (" + player.getGameProfile().getId() + ")";
        }
        if (isOwner) {
            Gravestones.LOGGER.info("{}{} has found their grave at {}", player.getName().getString(), uuid, pos.toString());
        } else {
            Gravestones.LOGGER.info("{}{} has found {}{}'s grave at {}",
                    player.getName().getString(), uuid,
                    graveOwner.name().orElse("???"), graveOwner.uuid().orElse(null),
                    pos.toString()
            );
        }

        GravestonesApi.onCollect(world, pos, player, gravestone.getDecay(), gravestone.getContents());
        gravestone.setContents(new NbtCompound());

        player.incrementStat(GravestonesRegistry.GRAVESTONES_COLLECTED);
        MinecraftServer server = world.getServer();
        if (server != null && GravestonesConfig.BROADCAST_COLLECT_IN_CHAT.getValue()) {
            MutableText text;
            if (GravestonesConfig.BROADCAST_COORDINATES_IN_CHAT.getValue()) {
                if (isOwner) {
                    text = Text.translatable("gravestones.player_collected_grave_at_coords", player.getName().getString(), pos.toString());
                } else {
                    text = Text.translatable("gravestones.player_collected_others_grave_at_coords", player.getName().getString(), graveOwner.name().orElse("???"), pos.toString());
                }
            } else {
                if (isOwner) {
                    text = Text.translatable("gravestones.player_collected_grave", player.getName().getString());
                } else {
                    text = Text.translatable("gravestones.player_collected_others_grave", player.getName().getString(), graveOwner.name().orElse("???"));
                }
            }
            server.getPlayerManager().broadcast(text, false);
        }
        world.breakBlock(pos, true);

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);

        if (state.getBlock() != world.getBlockState(pos).getBlock()) {
            createSoulParticles(world, pos);
        }
    }

    public static void createSoulParticles(World world, BlockPos pos) {
        Random random = new Random();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 16; ++i) {
                serverWorld.spawnParticles(ParticleTypes.SOUL, pos.getX() + (random.nextFloat() * 0.6) + 0.2, pos.getY() + (random.nextFloat() / 10) + 0.25, pos.getZ() + (random.nextFloat() * 0.6) + 0.2, 1, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, 0.1);
            }
        }
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {

    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TechnicalGravestoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TechnicalGravestoneBlock.validateTicker(type, GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, TechnicalGravestoneBlockEntity::tick);
    }
}