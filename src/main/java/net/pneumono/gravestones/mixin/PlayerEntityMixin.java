package net.pneumono.gravestones.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.pneumono.gravestones.gravestones.GravestoneCreation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
@SuppressWarnings("unused")
public abstract class PlayerEntityMixin {
    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    public void spawnGravestone(CallbackInfo ci) {
        GravestoneCreation.handleGravestones((PlayerEntity)(Object)this);
        ci.cancel();
    }
}