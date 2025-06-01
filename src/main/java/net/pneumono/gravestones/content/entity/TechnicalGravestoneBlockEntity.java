package net.pneumono.gravestones.content.entity;

import net.minecraft.block.BlockState;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pneumono.gravestones.Gravestones;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.content.GravestoneSkeletonEntity;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.content.TechnicalGravestoneBlock;
import net.pneumono.gravestones.gravestones.enums.DecayTimeType;
import net.pneumono.gravestones.gravestones.GravestoneTime;

import java.util.*;

public class TechnicalGravestoneBlockEntity extends AbstractGravestoneBlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(127, ItemStack.EMPTY);
    private int experience;
    private NbtList modData;
    private ProfileComponent graveOwner;
    private String spawnDateTime;
    private long spawnDateTicks;

    public TechnicalGravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, pos, state);
        this.modData = new NbtList();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, this.inventory, registryLookup);
        nbt.putInt("experience", this.experience);
        if (this.graveOwner != null) {
            nbt.put("owner", ProfileComponent.CODEC.encodeStart(NbtOps.INSTANCE, this.graveOwner).getOrThrow());
        }
        if (this.spawnDateTime != null) {
            nbt.putString("spawnDateTime", this.spawnDateTime);
        }
        if (this.spawnDateTicks != 0) {
            nbt.putLong("spawnDateTicks", this.spawnDateTicks);
        }
        if (this.modData != null) {
            nbt.put("modData", this.modData);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, this.inventory, registryLookup);
        this.experience = nbt.getInt("experience", 0);
        ProfileComponent.CODEC.parse(NbtOps.INSTANCE, nbt.get("owner")).resultOrPartial(string -> Gravestones.LOGGER.error("Failed to load profile from gravestone: {}", string)).ifPresent(this::setGraveOwner);
        this.spawnDateTime = nbt.getString("spawnDateTime", null);
        this.spawnDateTicks = nbt.getLong("spawnDateTicks", 0);
        this.modData = nbt.getListOrEmpty("modData");
    }

    public static void tick(World world, BlockPos blockPos, BlockState state, TechnicalGravestoneBlockEntity entity) {
        if (!world.isClient() && world.getTime() % 20 == 0) {
            if (GravestonesConfig.DECAY_WITH_TIME.getValue() && entity.getGraveOwner() != null) {
                long difference;

                if (GravestonesConfig.GRAVESTONE_DECAY_TIME_TYPE.getValue() == DecayTimeType.TICKS) {
                    difference = world.getTime() - entity.spawnDateTicks;
                } else if (entity.spawnDateTime != null) {
                    difference = GravestoneTime.getDifferenceInSeconds(GravestoneTime.READABLE.format(new Date()), entity.spawnDateTime) * 20;
                } else {
                    difference = 0;
                }

                long timeUnit = GravestonesConfig.DECAY_TIME.getValue();
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

        if (GravestonesConfig.SPAWN_GRAVESTONE_SKELETONS.getValue() && world.getTime() % 900 == 0 && isOwnerNearby(world, entity, blockPos)) {
            spawnSkeletons(world, entity, blockPos);
        }
    }

    private static boolean isOwnerNearby(World world, TechnicalGravestoneBlockEntity entity, BlockPos blockPos) {
        ProfileComponent profileComponent = entity.getGraveOwner();
        if (profileComponent == null) {
            return false;
        }

        Box box = Box.enclosing(blockPos.down(30).south(50).west(50), blockPos.up(30).north(50).east(50));
        for (Entity nearbyEntity : world.getOtherEntities(null, box)) {
            if (nearbyEntity instanceof PlayerEntity player && player.getGameProfile().getId() == profileComponent.gameProfile().getId()) {
                return true;
            }
        }

        return false;
    }

    private static void spawnSkeletons(World world, TechnicalGravestoneBlockEntity entity, BlockPos blockPos) {
        int entityCount = entity.countEntities(world);

        if (entityCount >= 5) {
            return;
        }

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
        while (!possiblePos.isEmpty()) {
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
        TechnicalGravestoneBlock.createSoulParticles(world, finalPos);
        TechnicalGravestoneBlock.createSoulParticles(world, blockPos);
    }

    private int countEntities(World world) {
        Box box = Box.enclosing(getPos().down(15).south(15).west(15), getPos().up(15).north(15).east(15));
        List<Entity> entities = world.getOtherEntities(null, box);
        int entityCount = 0;
        for (Entity nearbyEntity : entities) {
            if (nearbyEntity instanceof GravestoneSkeletonEntity) {
                entityCount++;
            }
        }
        return entityCount;
    }

    public void setGraveOwner(ProfileComponent graveOwner) {
        this.graveOwner = graveOwner;
        this.markDirty();
    }

    public ProfileComponent getGraveOwner() {
        return this.graveOwner;
    }

    public void setSpawnDate(String spawnDateTime, long spawnDateTicks) {
        this.spawnDateTime = spawnDateTime;
        this.spawnDateTicks = spawnDateTicks;
        this.markDirty();
    }

    public String getSpawnDateTime() {
        return this.spawnDateTime;
    }

    public int getExperience() {
        return experience;
    }

    public int getExperienceToDrop(BlockState state) {
        int experience = getExperience();
        if (GravestonesConfig.EXPERIENCE_DECAY.getValue()) {
            return experience / (state.get(TechnicalGravestoneBlock.DAMAGE) + 1);
        } else {
            return experience;
        }
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * Adds data from other mods to the gravestone. If the mod ID is already in use, replaces the data.
     *
     * @param modID The mod ID of the mod adding data.
     * @param data The data being added.
     */
    public void addOrReplaceModData(String modID, NbtCompound data) {
        NbtList newData = new NbtList();
        for (NbtElement element : modData) {
            if (!(element instanceof NbtCompound compound && compound.contains("modID") && Objects.equals(compound.getString("modID").orElse(null), modID))) {
                newData.add(element);
            }
        }

        NbtCompound compound = new NbtCompound();
        compound.putString("modID", modID);
        compound.put("data", data);

        newData.add(compound);
        modData = newData;
    }

    /**
     * Gets data added from other mods from the gravestone.
     *
     * @param modID The mod ID of the mod that added the data.
     * @return The data previously added.
     */
    public NbtCompound getModData(String modID) {
        for (int i = 0; i < modData.size(); ++i) {
            NbtCompound nbt = modData.getCompound(i).orElse(null);
            if (nbt != null && nbt.contains("modID") && Objects.equals(nbt.getString("modID").orElse(null), modID)) {
                return nbt.getCompound("data").orElse(nbt);
            }
        }
        return null;
    }

    public NbtList getAllModData() {
        return modData;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public Direction getGravestoneDirection() {
        return Direction.NORTH;
    }
}