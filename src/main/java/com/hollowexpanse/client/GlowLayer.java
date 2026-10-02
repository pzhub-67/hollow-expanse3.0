package com.hollowexpanse.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Draws a texture at full brightness over the model: eyes, chest cores, glowing seams. */
public class GlowLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private final RenderType renderType;

    public GlowLayer(RenderLayerParent<T, M> parent, ResourceLocation glowTexture) {
        super(parent);
        this.renderType = RenderType.eyes(glowTexture);
    }

    @Override
    public RenderType renderType() {
        return renderType;
    }
}
