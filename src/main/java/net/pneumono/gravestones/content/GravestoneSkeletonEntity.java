package net.pneumono.gravestones.content;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.world.World;

public class GravestoneSkeletonEntity extends SkeletonEntity {
    public GravestoneSkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    public GravestoneSkeletonEntity(World world) {
        super(GravestonesRegistry.GRAVESTONE_SKELETON_ENTITY_TYPE, world);
    }

    @Override
    public boolean shouldDropXp() {
        return false;
    }

    @Override
    protected void drop(DamageSource source) {}

    @Override
    public void tick() {
        super.tick();
        if (isAlive() && age > 1200 && age % 20 == 0) {
            damage(getDamageSources().starve(), 2);
        }
    }
}
