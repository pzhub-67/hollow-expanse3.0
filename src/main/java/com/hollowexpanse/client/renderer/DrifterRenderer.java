package com.hollowexpanse.client.renderer;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.client.GlowLayer;
import com.hollowexpanse.entity.DrifterEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** Reuses the Ghast model (bell + tentacles = a drifting jelly) at 40% size, fully self-lit. */
public class DrifterRenderer extends MobRenderer<DrifterEntity, GhastModel<DrifterEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "textures/entity/drifter.png");
    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "textures/entity/drifter_glow.png");

    public DrifterRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel<>(context.bakeLayer(ModelLayers.GHAST)), 0.3F);
        this.addLayer(new GlowLayer<>(this, GLOW));
    }

    @Override
    public ResourceLocation getTextureLocation(DrifterEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(DrifterEntity entity, PoseStack poseStack, float partialTick) {
        float s = 0.4F;
        poseStack.scale(s, s, s);
        poseStack.translate(0.0F, 1.0F, 0.0F);
    }

    @Override
    protected int getBlockLightLevel(DrifterEntity entity, BlockPos pos) {
        return 15;
    }
}
