package net.pneumono.gravestones.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.explosion.Explosion;
import net.pneumono.gravestones.content.GravestonesRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Explosion.class)
@SuppressWarnings("unused")
public abstract class ExplosionMixin {
    @ModifyVariable(method = "affectWorld", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private BlockState modifyAffectedBlocks(BlockState old) {
        if (old.getBlock() == GravestonesRegistry.GRAVESTONE_TECHNICAL) {
            return Blocks.AIR.getDefaultState();
        }

        return old;
    }
}
