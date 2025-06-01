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
import net.pneumono.gravestones.gravestones.GravestoneDecay;

import java.util.*;

public class TechnicalGravestoneBlockEntity extends AbstractGravestoneBlockEntity implements ImplementedInventory {
    private static final int CURRENT_FORMAT_VERSION = 1;
    private int format;
    private NbtCompound contents;
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
        nbt.putInt("format", this.format);
        nbt.put("contents", this.contents);
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
        this.format = nbt.getInt("format", 1);
        this.contents = nbt.getCompoundOrEmpty("contents");
        Inventories.readNbt(nbt, this.inventory, registryLookup);
        this.experience = nbt.getInt("experience", 0);
        ProfileComponent.CODEC.parse(NbtOps.INSTANCE, nbt.get("owner")).resultOrPartial(string -> Gravestones.LOGGER.error("Failed to load profile from gravestone: {}", string)).ifPresent(this::setGraveOwner);
        this.spawnDateTime = nbt.getString("spawnDateTime", null);
        this.spawnDateTicks = nbt.getLong("spawnDateTicks", 0);
        this.modData = nbt.getListOrEmpty("modData");
    }

    public static void tick(World world, BlockPos blockPos, BlockState state, TechnicalGravestoneBlockEntity entity) {
        if (world.isClient() || world.getTime() % 20 != 0) {
            return;
        }

        GravestoneDecay.timeDecayGravestone(world, blockPos, state);

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

    public int getDecay() {
        return Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()).get(TechnicalGravestoneBlock.DAMAGE);
    }

    public NbtCompound getContents() {
        return this.contents;
    }

    public void setContents(NbtCompound contents) {
        this.contents = contents;
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

    public long getSpawnDateTicks() {
        return this.spawnDateTicks;
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