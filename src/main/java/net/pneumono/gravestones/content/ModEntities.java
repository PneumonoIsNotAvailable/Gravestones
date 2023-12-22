package net.pneumono.gravestones.content;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.pneumono.gravestones.Gravestones;

public class ModEntities {
    public static final EntityType<GravestoneSkeletonEntity> GRAVESTONE_SKELETON_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Gravestones.MOD_ID, "gravestone_skeleton"),
            FabricEntityTypeBuilder.<GravestoneSkeletonEntity>create(SpawnGroup.MISC, GravestoneSkeletonEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.99F))
                    .trackRangeBlocks(8)
                    .build()
    );

    public static void registerModEntities() {
        FabricDefaultAttributeRegistry.register(ModEntities.GRAVESTONE_SKELETON_ENTITY_TYPE, GravestoneSkeletonEntity.createAbstractSkeletonAttributes());
    }
}
