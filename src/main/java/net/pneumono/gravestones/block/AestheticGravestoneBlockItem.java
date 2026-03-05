package net.pneumono.gravestones.block;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.pneumono.gravestones.GravestonesConfig;

public class AestheticGravestoneBlockItem extends BlockItem {
    public AestheticGravestoneBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return GravestonesConfig.AESTHETIC_GRAVESTONES.getValue() && super.isEnabled(enabledFeatures);
    }
}
