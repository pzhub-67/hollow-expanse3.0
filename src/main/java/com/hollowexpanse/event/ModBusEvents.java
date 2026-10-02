package com.hollowexpanse.event;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.entity.DrifterEntity;
import com.hollowexpanse.entity.HollowWardenEntity;
import com.hollowexpanse.entity.HuskStalkerEntity;
import com.hollowexpanse.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HollowExpanse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBusEvents {
    private ModBusEvents() {}

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.HOLLOW_WARDEN.get(), HollowWardenEntity.createAttributes().build());
        event.put(ModEntities.DRIFTER.get(), DrifterEntity.createAttributes().build());
        event.put(ModEntities.HUSK_STALKER.get(), HuskStalkerEntity.createAttributes().build());
    }
}
