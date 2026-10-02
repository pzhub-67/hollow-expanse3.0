package com.hollowexpanse.registry;

import com.hollowexpanse.HollowExpanse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Dimension keys. The dimensions themselves are data-driven (data/hollow_expanse/dimension). */
public final class ModDimensions {
    private ModDimensions() {}

    public static final ResourceKey<Level> HOLLOW_EXPANSE = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "hollow_expanse"));
    public static final ResourceKey<Level> UNDERLAYER = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "underlayer"));

    public static boolean isHollow(Level level) {
        return level.dimension() == HOLLOW_EXPANSE || level.dimension() == UNDERLAYER;
    }
}
