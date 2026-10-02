package com.hollowexpanse.client;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.client.renderer.DrifterRenderer;
import com.hollowexpanse.client.renderer.HollowWardenRenderer;
import com.hollowexpanse.client.renderer.HuskStalkerRenderer;
import com.hollowexpanse.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HollowExpanse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOLLOW_WARDEN.get(), HollowWardenRenderer::new);
        event.registerEntityRenderer(ModEntities.DRIFTER.get(), DrifterRenderer::new);
        event.registerEntityRenderer(ModEntities.HUSK_STALKER.get(), HuskStalkerRenderer::new);
        // The shard is a self-lit billboard of its item sprite.
        event.registerEntityRenderer(ModEntities.VOID_SHARD.get(),
                context -> new ThrownItemRenderer<>(context, 1.4F, true));
    }
}
