package net.pneumono.gravestones.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.api.GravestonesApi;
import net.pneumono.gravestones.content.GravestoneSkeletonEntity;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.gravestones.GravestoneDecay;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TechnicalGravestoneBlockEntity extends AbstractGravestoneBlockEntity {
    private NbtCompound contents = new NbtCompound();
    @Nullable
    private ProfileComponent graveOwner;
    private String spawnDateTime;
    private long spawnDateTicks;
    private int deathDamage = 0;
    private int ageDamage = 0;

    public TechnicalGravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, pos, state);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        // Scuffed, will move away from NBT in future if possible
        view.put("contents", NbtCompound.CODEC, this.contents);
        view.putNullable("owner", ProfileComponent.CODEC, this.graveOwner);
        if (this.spawnDateTime != null) {
            view.putString("spawnDateTime", this.spawnDateTime);
        }
        if (this.spawnDateTicks != 0) {
            view.putLong("spawnDateTicks", this.spawnDateTicks);
        }
        view.putInt("deathDamage", this.deathDamage);
        view.putInt("ageDamage", this.ageDamage);
    }

    @Override
    public void readData(ReadView view) {
        super.readData(view);
        // Scuffed, will move away from NBT in future if possible
        this.contents = view.read("contents", NbtCompound.CODEC).orElse(new NbtCompound());
        this.setGraveOwner(view.read("owner", ProfileComponent.CODEC).orElse(null));
        this.spawnDateTime = view.getString("spawnDateTime", null);
        this.spawnDateTicks = view.getLong("spawnDateTicks", 0);
        this.deathDamage = view.getInt("deathDamage", 0);
        this.ageDamage = view.getInt("ageDamage", 0);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        GravestonesApi.onBreak(getWorld(), pos, getDecay(oldState), this);
        super.onBlockReplaced(pos, oldState);
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
        return getDecay(Objects.requireNonNull(this.getWorld()).getBlockState(this.getPos()));
    }

    public static int getDecay(BlockState state) {
        return state.get(TechnicalGravestoneBlock.DAMAGE);
    }

    public NbtCompound getContents() {
        return this.contents;
    }

    public void setContents(NbtCompound contents) {
        this.contents = contents;
        this.markDirty();
    }

    public void setGraveOwner(ProfileComponent graveOwner) {
        synchronized (this) {
            this.graveOwner = graveOwner;
        }

        if (this.graveOwner != null && !this.graveOwner.isCompleted()) {
            this.graveOwner.getFuture().thenAcceptAsync(owner -> {
                this.graveOwner = owner;
                this.markDirty();
            }, SkullBlockEntity.EXECUTOR);
        } else {
            this.markDirty();
        }
    }

    public int getTotalDamage() {
        return this.ageDamage + this.deathDamage;
    }

    public int getAgeDamage() {
        return this.ageDamage;
    }

    public void setAgeDamage(int ageDamage) {
        if (this.getAgeDamage() != ageDamage) {
            this.ageDamage = ageDamage;
            this.markDirty();
        }
    }

    public int getDeathDamage() {
        return this.deathDamage;
    }

    public void setDeathDamage(int deathDamage) {
        this.deathDamage = deathDamage;
        this.markDirty();
    }

    @Nullable
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

    @Override
    public Direction getGravestoneDirection() {
        return Direction.NORTH;
    }
}