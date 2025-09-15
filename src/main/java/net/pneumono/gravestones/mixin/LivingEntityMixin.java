package net.pneumono.gravestones.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pneumono.gravestones.gravestones.GravestoneCreation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "drop", at = @At("HEAD"))
    public void spawnGravestone(ServerWorld world, DamageSource damageSource, CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity player) {
            GravestoneCreation.create(player);
        }
    }
}