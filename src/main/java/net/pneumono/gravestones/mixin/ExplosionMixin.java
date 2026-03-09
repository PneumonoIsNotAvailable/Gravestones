package net.pneumono.gravestones.mixin;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    //? if <1.20.3 {
    /*@ModifyVariable(method = "finalizeExplosion", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private BlockState modifyAffectedBlocks(BlockState old) {
        if (old.getBlock() == GravestonesRegistry.GRAVESTONE_TECHNICAL) {
            return Blocks.AIR.defaultBlockState();
        }
        return old;
    }
    *///?}
}
