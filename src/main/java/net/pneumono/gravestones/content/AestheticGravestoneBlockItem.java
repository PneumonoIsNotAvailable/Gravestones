package net.pneumono.gravestones.content;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.pneumono.gravestones.Gravestones;

public class AestheticGravestoneBlockItem extends BlockItem {
    public AestheticGravestoneBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public boolean isEnabled(FeatureSet enabledFeatures) {
        return Gravestones.AESTHETIC_GRAVESTONES.getValue() && super.isEnabled(enabledFeatures);
    }
}
