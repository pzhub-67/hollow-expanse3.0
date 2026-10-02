package com.hollowexpanse.client.renderer;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.client.GlowLayer;
import com.hollowexpanse.entity.HuskStalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class HuskStalkerRenderer extends HumanoidMobRenderer<HuskStalkerEntity, HumanoidModel<HuskStalkerEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "textures/entity/husk_stalker.png");
    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "textures/entity/husk_stalker_glow.png");

    public HuskStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        this.addLayer(new GlowLayer<>(this, GLOW));
    }

    @Override
    public ResourceLocation getTextureLocation(HuskStalkerEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(HuskStalkerEntity entity, BlockPos pos) {
        return Math.max(8, super.getBlockLightLevel(entity, pos));
    }
}
