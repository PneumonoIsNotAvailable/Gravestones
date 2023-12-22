package net.pneumono.gravestones.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.pneumono.gravestones.content.ModBlocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @ModifyVariable(method = "affectWorld", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private BlockState modifyAffectedBlocks(BlockState old) {
        if (old.getBlock() == ModBlocks.GRAVESTONE_TECHNICAL) {
            return Blocks.AIR.getDefaultState();
        }

        return old;
    }
}
