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
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    protected void drop(ServerWorld world, DamageSource damageSource) {}

    @Override
    public void tick() {
        super.tick();
        if (MultiVersionUtil.getWorld(this) instanceof ServerWorld world && isAlive() && age > 1200 && age % 20 == 0) {
            damage(world, getDamageSources().starve(), 2);
        }
    }
}
