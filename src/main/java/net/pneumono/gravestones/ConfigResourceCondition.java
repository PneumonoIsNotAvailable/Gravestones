package net.pneumono.gravestones;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.registry.RegistryOps;
import org.jetbrains.annotations.Nullable;

// sucks and is terrible but works for now
// i'll make a better one later i swear it just needs to work
public class ConfigResourceCondition implements ResourceCondition {
    public static final MapCodec<ConfigResourceCondition> CODEC = MapCodec.unit(ConfigResourceCondition::new);

    @Override
    public ResourceConditionType<?> getType() {
        return Gravestones.RESOURCE_CONDITION_CONFIGURATIONS;
    }

    @Override
    public boolean test(@Nullable RegistryOps.RegistryInfoGetter registryInfoGetter) {
        return Gravestones.AESTHETIC_GRAVESTONES.getValue();
    }
}
