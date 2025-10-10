package net.pneumono.gravestones.content;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.pneumono.pneumonocore.util.MultiVersionUtil;

public class GravestoneSkeletonEntity extends SkeletonEntity {
    public GravestoneSkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    public GravestoneSkeletonEntity(World world) {
        super(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, world);
    }

    @Override
    //? if >=1.21.4 {
    public boolean shouldDropExperience() {
        return false;
    }
    //?} else {
    /*public boolean isExperienceDroppingDisabled() {
        return true;
    }
    *///?}

    @Override
    protected void drop(/*? if >=1.21.1 {*/ServerWorld world,/*?}*/ DamageSource damageSource) {}

    @Override
    public void tick() {
        super.tick();
        if (MultiVersionUtil.getWorld(this) instanceof ServerWorld world && isAlive() && age > 1200 && age % 20 == 0) {
            //? if >=1.21.3 {
            damage(world, getDamageSources().starve(), 2);
            //?} else {
            /*damage(getDamageSources().starve(), 2);
            *///?}
        }
    }
}
