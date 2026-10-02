package com.hollowexpanse.client.renderer;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.client.GlowLayer;
import com.hollowexpanse.entity.HollowWardenEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class HollowWardenRenderer extends HumanoidMobRenderer<HollowWardenEntity, HumanoidModel<HollowWardenEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "textures/entity/hollow_warden.png");
    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "textures/entity/hollow_warden_glow.png");

    public HollowWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 1.6F);
        this.addLayer(new GlowLayer<>(this, GLOW));
    }

    @Override
    public ResourceLocation getTextureLocation(HollowWardenEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(HollowWardenEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.9F, 1.9F, 1.9F);
    }

    @Override
    protected int getBlockLightLevel(HollowWardenEntity entity, BlockPos pos) {
        return 15;
    }
}
