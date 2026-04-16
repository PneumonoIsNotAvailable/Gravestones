package net.pneumono.gravestones.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.pneumono.gravestones.GravestonesConfig;
import net.pneumono.gravestones.content.GravestoneSkeletonEntity;
import net.pneumono.gravestones.content.GravestonesRegistry;
import net.pneumono.gravestones.gravestones.GravestoneDecay;
import net.pneumono.gravestones.gravestones.GravestoneManager;
import net.pneumono.gravestones.multiversion.GraveOwner;
import net.pneumono.gravestones.multiversion.VersionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//? if <1.21.9 {
/*import net.minecraft.world.level.block.entity.SkullBlockEntity;
*///?}

//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
*///?}

//? if >=1.21.5 {
import net.minecraft.server.level.ServerLevel;
import net.pneumono.gravestones.api.GravestonesApi;
//?}

//? if <1.20.5 {
/*import net.minecraft.Util;
*///?}

public class TechnicalGravestoneBlockEntity extends AbstractGravestoneBlockEntity {
    private CompoundTag contents = new CompoundTag();
    @Nullable
    private GraveOwner graveOwner;
    private String spawnDateTime;
    private long spawnDateTicks;
    private int deathDamage = 0;
    private int ageDamage = 0;

    public TechnicalGravestoneBlockEntity(BlockPos pos, BlockState state) {
        super(GravestonesRegistry.TECHNICAL_GRAVESTONE_ENTITY, pos, state);
    }

    //? if >=1.21.6 {
    @Override
    public void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("contents", CompoundTag.CODEC, this.contents);
        view.storeNullable("owner", GraveOwner.CODEC, this.graveOwner);
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
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.contents = view.read("contents", CompoundTag.CODEC).orElse(new CompoundTag());
        this.setGraveOwner(view.read("owner", GraveOwner.CODEC).orElse(null));
        this.spawnDateTime = view.getStringOr("spawnDateTime", null);
        this.spawnDateTicks = view.getLongOr("spawnDateTicks", 0);
        this.deathDamage = view.getIntOr("deathDamage", 0);
        this.ageDamage = view.getIntOr("ageDamage", 0);
    }
    //?} else if >=1.20.5 {
    /*@Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);

        VersionUtil.put(ops, tag, "contents", CompoundTag.CODEC, this.contents);
        if (this.graveOwner != null) {
            VersionUtil.put(ops, tag, "owner", GraveOwner.CODEC, this.graveOwner);
        }
        if (this.spawnDateTime != null) {
            tag.putString("spawnDateTime", this.spawnDateTime);
        }
        if (this.spawnDateTicks != 0) {
            tag.putLong("spawnDateTicks", this.spawnDateTicks);
        }
        tag.putInt("deathDamage", this.deathDamage);
        tag.putInt("ageDamage", this.ageDamage);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);

        this.contents = VersionUtil.get(ops, tag, "contents", CompoundTag.CODEC).orElse(new CompoundTag());
        this.setGraveOwner(VersionUtil.get(ops, tag, "owner", GraveOwner.CODEC).orElse(null));
        this.spawnDateTime = tag.getString("spawnDateTime")/^? if >=1.21.5 {^//^.orElse(null)^//^?}^/;
        this.spawnDateTicks = tag.getLong("spawnDateTicks")/^? if >=1.21.5 {^//^.orElse(0L)^//^?}^/;
        this.deathDamage = tag.getInt("deathDamage")/^? if >=1.21.5 {^//^.orElse(0)^//^?}^/;
        this.ageDamage = tag.getInt("ageDamage")/^? if >=1.21.5 {^//^.orElse(0)^//^?}^/;
    }
    *///?} else {
    /*@Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        VersionUtil.put(tag, "contents", CompoundTag.CODEC, this.contents);
        if (this.graveOwner != null) {
            VersionUtil.put(tag, "owner", GraveOwner.CODEC, this.graveOwner);
        }
        if (this.spawnDateTime != null) {
            tag.putString("spawnDateTime", this.spawnDateTime);
        }
        if (this.spawnDateTicks != 0) {
            tag.putLong("spawnDateTicks", this.spawnDateTicks);
        }
        tag.putInt("deathDamage", this.deathDamage);
        tag.putInt("ageDamage", this.ageDamage);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.contents = VersionUtil.get(tag, "contents", CompoundTag.CODEC).orElse(new CompoundTag());
        this.setGraveOwner(VersionUtil.get(tag, "owner", GraveOwner.CODEC).orElse(null));
        this.spawnDateTime = tag.getString("spawnDateTime");
        this.spawnDateTicks = tag.getLong("spawnDateTicks");
        this.deathDamage = tag.getInt("deathDamage");
        this.ageDamage = tag.getInt("ageDamage");
    }
    *///?}

    //? if >=1.21.5 {
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        if (getLevel() instanceof ServerLevel serverLevel) {
            GravestoneManager.info("Breaking Gravestone at ({})", pos.toShortString());
            GravestonesApi.onBreak(serverLevel, pos, getTotalDamage(), this);
        }
        super.preRemoveSideEffects(pos, oldState);
    }
    //?}

    public static void tick(Level level, BlockPos blockPos, BlockState state, TechnicalGravestoneBlockEntity entity) {
        if (level.isClientSide() || level.getGameTime() % 20 != 0) {
            return;
        }

        GravestoneDecay.timeDecayGravestone(level, blockPos, state);

        if (GravestonesConfig.SPAWN_GRAVESTONE_SKELETONS.getValue() && level.getGameTime() % 900 == 0 && isOwnerNearby(level, entity, blockPos)) {
            spawnSkeletons(level, entity, blockPos);
        }
    }

    private static boolean isOwnerNearby(Level level, TechnicalGravestoneBlockEntity entity, BlockPos blockPos) {
        GraveOwner graveOwner = entity.getGraveOwner();
        if (graveOwner == null) {
            return false;
        }

        AABB box = /*? if >=1.20.3 {*/AABB.encapsulatingFullBlocks/*?} else {*//*new AABB*//*?}*/(blockPos.below(30).south(50).west(50), blockPos.above(30).north(50).east(50));
        for (Entity nearbyEntity : level.getEntities(null, box)) {
            if (nearbyEntity instanceof Player player && VersionUtil.getId(player.getGameProfile()).equals(graveOwner.getUuid())) {
                return true;
            }
        }

        return false;
    }

    private static void spawnSkeletons(Level level, TechnicalGravestoneBlockEntity entity, BlockPos blockPos) {
        int entityCount = entity.countEntities(level);

        if (entityCount >= 5) {
            return;
        }

        GravestoneSkeletonEntity spawned = new GravestoneSkeletonEntity(level);

        List<BlockPos> possiblePos = new ArrayList<>();
        for (int x = -5; x < 6; ++x) {
            for (int y = -5; y < 6; ++y) {
                for (int z = -5; z < 6; ++z) {
                    possiblePos.add(new BlockPos(entity.getBlockPos().getX() + x, entity.getBlockPos().getY() + y, entity.getBlockPos().getZ() + z));
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
            while (level.getBlockState(possible.below()).isAir() && !tooFar) {
                if (possible.below().getY() < blockPos.below(5).getY()) {
                    tooFar = true;
                }
                possible = possible.below();
            }

            if (tooFar) {
                continue;
            }

            if (level.getBlockState(possible).isAir() && level.getBlockState(possible.above()).isAir()) {
                finalPos = possible;
                break;
            }
        }

        if (finalPos == null) {
            finalPos = blockPos;
        }

        spawned.setPosRaw(finalPos.getX() + 0.5, finalPos.getY() + 0.1, finalPos.getZ() + 0.5);
        spawned.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1));
        if (random.nextFloat() > 0.5) {
            spawned.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
            spawned.setItemSlot(EquipmentSlot.HEAD, Items.LEATHER_HELMET.getDefaultInstance());
        } else {
            spawned.setItemSlot(EquipmentSlot.HEAD, Items.IRON_HELMET.getDefaultInstance());
        }
        spawned.setDropChance(EquipmentSlot.MAINHAND, 0);
        spawned.setDropChance(EquipmentSlot.HEAD, 0);

        level.addFreshEntity(spawned);
        TechnicalGravestoneBlock.createSoulParticles(level, finalPos);
        TechnicalGravestoneBlock.createSoulParticles(level, blockPos);
    }

    private int countEntities(Level level) {
        AABB box = /*? if >=1.20.3 {*/AABB.encapsulatingFullBlocks/*?} else {*//*new AABB*//*?}*/(getBlockPos().below(15).south(15).west(15), getBlockPos().above(15).north(15).east(15));
        List<Entity> entities = level.getEntities(null, box);
        int entityCount = 0;
        for (Entity nearbyEntity : entities) {
            if (nearbyEntity instanceof GravestoneSkeletonEntity) {
                entityCount++;
            }
        }
        return entityCount;
    }

    /**
     * @return The decay stage of the Gravestone. NOT the total damage.
     */
    public int getDecay() {
        return getDecay(Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()));
    }

    /**
     * @return The decay stage of the Gravestone. NOT the total damage.
     */
    public static int getDecay(BlockState state) {
        return state.getValue(TechnicalGravestoneBlock.DAMAGE);
    }

    public CompoundTag getContents() {
        return this.contents;
    }

    public void setContents(CompoundTag contents) {
        this.contents = contents;
        this.setChanged();
    }

    public void setGraveOwner(@Nullable GraveOwner graveOwner) {
        this.graveOwner = graveOwner;

        //? if >=1.21.9 {
        this.setChanged();
        //?} else if >=1.20.5 {
        /*if (this.graveOwner != null && !this.graveOwner.getProfile().isResolved()) {
            this.graveOwner.getProfile().resolve().thenAcceptAsync(profile -> {
                this.graveOwner.setProfile(profile);
                this.setChanged();
            }, SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR);
        } else {
            this.setChanged();
        }
        *///?} else if >=1.20.2 {
        /*if (this.graveOwner != null && !Util.isBlank(this.graveOwner.getName()) && !SkullBlockEntity.hasTextures(this.graveOwner.getProfile())) {
            SkullBlockEntity.fetchGameProfile(this.graveOwner.getName()).thenAcceptAsync(profile -> {
                this.graveOwner.setProfile(profile.orElse(this.graveOwner.getProfile()));
                this.setChanged();
            }, SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR);
        } else {
            this.setChanged();
        }
        *///?} else {
        /*SkullBlockEntity.updateGameprofile(this.graveOwner == null ? null : this.graveOwner.getProfile(), profile -> {
            this.graveOwner.setProfile(profile);
            this.setChanged();
        });
        *///?}
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
            this.setChanged();
        }
    }

    public int getDeathDamage() {
        return this.deathDamage;
    }

    public void setDeathDamage(int deathDamage) {
        this.deathDamage = deathDamage;
        this.setChanged();
    }

    @Nullable
    public GraveOwner getGraveOwner() {
        return this.graveOwner;
    }

    public void setSpawnDate(String spawnDateTime, long spawnDateTicks) {
        this.spawnDateTime = spawnDateTime;
        this.spawnDateTicks = spawnDateTicks;
        this.setChanged();
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