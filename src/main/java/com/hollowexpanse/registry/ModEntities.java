package com.hollowexpanse.registry;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.entity.DrifterEntity;
import com.hollowexpanse.entity.HollowWardenEntity;
import com.hollowexpanse.entity.HuskStalkerEntity;
import com.hollowexpanse.entity.VoidShardEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HollowExpanse.MOD_ID);

    public static final RegistryObject<EntityType<HollowWardenEntity>> HOLLOW_WARDEN =
            ENTITY_TYPES.register("hollow_warden",
                    () -> EntityType.Builder.of(HollowWardenEntity::new, MobCategory.MONSTER)
                            .sized(1.8F, 3.4F).fireImmune().clientTrackingRange(10)
                            .build("hollow_warden"));

    public static final RegistryObject<EntityType<DrifterEntity>> DRIFTER =
            ENTITY_TYPES.register("drifter",
                    () -> EntityType.Builder.of(DrifterEntity::new, MobCategory.AMBIENT)
                            .sized(0.9F, 1.2F).clientTrackingRange(8)
                            .build("drifter"));

    public static final RegistryObject<EntityType<HuskStalkerEntity>> HUSK_STALKER =
            ENTITY_TYPES.register("husk_stalker",
                    () -> EntityType.Builder.of(HuskStalkerEntity::new, MobCategory.MONSTER)
                            .sized(0.7F, 2.0F).clientTrackingRange(10)
                            .build("husk_stalker"));

    public static final RegistryObject<EntityType<VoidShardEntity>> VOID_SHARD =
            ENTITY_TYPES.register("void_shard",
                    () -> EntityType.Builder.<VoidShardEntity>of(VoidShardEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(2)
                            .build("void_shard"));
}
