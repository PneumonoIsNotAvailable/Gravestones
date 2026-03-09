package net.pneumono.gravestones.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.pneumono.gravestones.gravestones.GravestoneCreation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21
import net.minecraft.server.level.ServerLevel;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // The mod should really be injecting later (in dropInventory),
    // but Accessories injects in dropEquipment for some reason,
    // and we need to be sure this is called before any other mods do their inventory dropping independently
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    //? if >=1.21 {
    public void spawnGravestone(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if ((Object)this instanceof Player player) {
            GravestoneCreation.create(player);
        }
    }
    //?} else {
    /*public void spawnGravestone(DamageSource source, CallbackInfo ci) {
        if ((Object)this instanceof Player player) {
            GravestoneCreation.create(player);
        }
    }
    *///?}
}