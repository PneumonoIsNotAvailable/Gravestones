package net.pneumono.gravestones.mixin;

import com.mojang.authlib.GameProfile;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.World;
import net.pneumono.gravestones.gravestones.GravestoneCreation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
@SuppressWarnings("unused")
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    private @Final GameProfile gameProfile;

    @Shadow
    private @Final PlayerInventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    public void spawnGravestone(CallbackInfo ci) {
        TrinketComponent trinketComponent = TrinketsApi.getTrinketComponent(inventory.player).orElse(null);
        GravestoneCreation.handleGravestones(getWorld(), getBlockPos(), getName().getString(), gameProfile, inventory, trinketComponent);
        ci.cancel();
    }
}