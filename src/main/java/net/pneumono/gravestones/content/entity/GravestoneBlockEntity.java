package net.pneumono.gravestones.content.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.content.GravestoneSkeletonEntity;
import net.pneumono.gravestones.content.GravestonesContent;
import net.pneumono.gravestones.content.TechnicalGravestoneBlock;
import net.pneumono.gravestones.gravestones.GravestoneTime;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GravestoneBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(127, ItemStack.EMPTY);
    private GameProfile graveOwner;
    private String spawnDate;

    public GravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(GravestonesContent.GRAVESTONE, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        if (this.graveOwner != null) {
            nbt.put("gravestone.owner", NbtHelper.writeGameProfile(new NbtCompound(), this.graveOwner));
        }
        if (this.spawnDate != null) {
            nbt.putString("gravestone.spawndate", this.spawnDate);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
        if (nbt.contains("gravestone.owner")) {
            this.graveOwner = NbtHelper.toGameProfile(nbt.getCompound("gravestone.owner"));
        }
        if (nbt.contains("gravestone.spawndate")) {
            this.spawnDate = nbt.getString("gravestone.spawndate");
        }
    }

    public static void tick(World world, BlockPos blockPos, BlockState state, GravestoneBlockEntity entity) {
        if (!world.isClient()) {
            if (Gravestones.GRAVESTONES_DECAY_WITH_TIME.getValue() && entity.spawnDate != null && entity.graveOwner != null) {
                long difference = GravestoneTime.getDifferenceInSeconds(GravestoneTime.getCurrentTimeAsString(), entity.spawnDate);
                // timeUnit default: 28800 (GravestoneTime.secondsInDay / 3)
                long timeUnit = Gravestones.GRAVESTONE_DECAY_TIME_HOURS.getValue() * 60 * 60;
                if (difference > (timeUnit * 3)) {
                    world.breakBlock(blockPos, true);
                } else if (difference > (timeUnit * 2) && !(state.get(TechnicalGravestoneBlock.AGE_DAMAGE) > 1)) {
                    world.setBlockState(blockPos, state.with(TechnicalGravestoneBlock.AGE_DAMAGE, 2));
                } else if (difference > (timeUnit) && !(state.get(TechnicalGravestoneBlock.AGE_DAMAGE) > 0)) {
                    world.setBlockState(blockPos, state.with(TechnicalGravestoneBlock.AGE_DAMAGE, 1));
                }

                markDirty(world, blockPos, state);
            }

            if (state.get(TechnicalGravestoneBlock.DAMAGE) != state.get(TechnicalGravestoneBlock.AGE_DAMAGE) + state.get(TechnicalGravestoneBlock.DEATH_DAMAGE)) {
                if (state.get(TechnicalGravestoneBlock.AGE_DAMAGE) + state.get(TechnicalGravestoneBlock.DEATH_DAMAGE) > 2) {
                    world.breakBlock(blockPos, true);
                } else {
                    world.setBlockState(blockPos, state.with(TechnicalGravestoneBlock.DAMAGE, state.get(TechnicalGravestoneBlock.AGE_DAMAGE) + state.get(TechnicalGravestoneBlock.DEATH_DAMAGE)));
                }
                markDirty(world, blockPos, state);
            }

            if (state.get(TechnicalGravestoneBlock.DAMAGE) >= 3) {
                world.breakBlock(blockPos, true);
                markDirty(world, blockPos, state);
            }
        }

        if (Gravestones.SPAWN_GRAVESTONE_SKELETONS.getValue()) {
            boolean ownerNearby = false;

            Box box = new Box(blockPos.down(30).south(50).west(50), blockPos.up(30).north(50).east(50));
            List<Entity> nearbyEntities = world.getOtherEntities(null, box);
            for (Entity nearbyEntity : nearbyEntities) {
                if (nearbyEntity instanceof PlayerEntity player && player.getGameProfile() == entity.graveOwner) {
                    ownerNearby = true;
                }
            }

            if (ownerNearby) {
                int entityCount = entity.countEntities(world);

                if (entityCount < 5 && world.getTime() % 900 == 0) {
                    GravestoneSkeletonEntity spawned = new GravestoneSkeletonEntity(world);

                    List<BlockPos> possiblePos = new ArrayList<>();
                    for (int x = -5; x < 6; ++x) {
                        for (int y = -5; y < 6; ++y) {
                            for (int z = -5; z < 6; ++z) {
                                possiblePos.add(new BlockPos(entity.getPos().getX() + x, entity.getPos().getY() + y, entity.getPos().getZ() + z));
                            }
                        }
                    }

                    Random random = new Random();
                    BlockPos finalPos = null;
                    while (possiblePos.size() > 0) {
                        int randInt = random.nextInt(possiblePos.size());
                        BlockPos possible = possiblePos.get(randInt);
                        possiblePos.remove(randInt);

                        boolean tooFar = false;
                        while (world.getBlockState(possible.down()).isAir() && !tooFar) {
                            if (possible.down().getY() < blockPos.down(5).getY()) {
                                tooFar = true;
                            }
                            possible = possible.down();
                        }

                        if (tooFar) {
                            continue;
                        }

                        if (world.getBlockState(possible).isAir() && world.getBlockState(possible.up()).isAir()) {
                            finalPos = possible;
                            break;
                        }
                    }

                    if (finalPos == null) {
                        finalPos = blockPos;
                    }

                    spawned.setPos(finalPos.getX() + 0.5, finalPos.getY() + 0.1, finalPos.getZ() + 0.5);
                    spawned.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, -1));
                    if (random.nextFloat() > 0.5) {
                        spawned.equipStack(EquipmentSlot.MAINHAND, Items.BOW.getDefaultStack());
                        spawned.equipStack(EquipmentSlot.HEAD, Items.LEATHER_HELMET.getDefaultStack());
                    } else {
                        spawned.equipStack(EquipmentSlot.HEAD, Items.IRON_HELMET.getDefaultStack());
                    }
                    spawned.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
                    spawned.setEquipmentDropChance(EquipmentSlot.HEAD, 0);

                    world.spawnEntity(spawned);
                }
            }
        }

        boolean ownerNearby = false;

        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        if (blockEntity instanceof GravestoneBlockEntity gravestone) {
            Box box = new Box(blockPos.down(5).south(5).west(5), blockPos.up(5).north(5).east(5));
            List<Entity> entities = world.getOtherEntities(null, box);
            for (Entity tempEntity : entities) {
                if (tempEntity instanceof PlayerEntity player && player.getGameProfile() == gravestone.getGraveOwner()) {
                    ownerNearby = true;
                }
            }
        }

        if (ownerNearby && !world.isClient() && world instanceof ServerWorld serverWorld) {
            Random random = new Random();
            serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, blockPos.getX() + (random.nextFloat() * 0.6) + 0.2, blockPos.getY() + (random.nextFloat() / 10) + 0.25, blockPos.getZ() + (random.nextFloat() * 0.6) + 0.2, 1, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, ((double) random.nextFloat() - 0.5) * 0.08, 0.1);
        }
    }

    private int countEntities(World world) {
        Box box = new Box(getPos().down(15).south(15).west(15), getPos().up(15).north(15).east(15));
        List<Entity> entities = world.getOtherEntities(null, box);
        int entityCount = 0;
        for (Entity nearbyEntity : entities) {
            if (nearbyEntity instanceof GravestoneSkeletonEntity) {
                entityCount++;
            }
        }
        return entityCount;
    }

    public List<ItemStack> getInventoryAsItemList() {
        return new ArrayList<>(inventory);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public void setGraveOwner(GameProfile graveOwner) {
        this.graveOwner = graveOwner;
        this.markDirty();
    }

    public GameProfile getGraveOwner() {
        return this.graveOwner;
    }

    public void setSpawnDate(String spawnDate) {
        this.spawnDate = spawnDate;
        this.markDirty();
    }

    public String getSpawnDate() {
        return this.spawnDate;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }
}