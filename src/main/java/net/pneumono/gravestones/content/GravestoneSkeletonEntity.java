package net.pneumono.gravestones.content;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

//? if >=1.21.11 {
import net.minecraft.world.entity.monster.skeleton.Skeleton;
//?} else {
/*import net.minecraft.world.entity.monster.Skeleton;
*///?}

public class GravestoneSkeletonEntity extends Skeleton {
    public GravestoneSkeletonEntity(EntityType<? extends Skeleton> entityType, Level level) {
        super(entityType, level);
    }

    public GravestoneSkeletonEntity(Level level) {
        super(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, level);
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    protected void dropAllDeathLoot(/*? if >=1.21 {*/ServerLevel level,/*?}*/ DamageSource damageSource) {}

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level && isAlive() && tickCount > 1200 && tickCount % 20 == 0) {
            //? if >=1.21.2 {
            hurtServer(level, damageSources().starve(), 2);
            //?} else {
            /*hurt(damageSources().starve(), 2);
            *///?}
        }
    }
}
