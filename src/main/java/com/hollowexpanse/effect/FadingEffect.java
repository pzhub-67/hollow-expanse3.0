package com.hollowexpanse.effect;

import com.hollowexpanse.HollowExpanse;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Each level of Fading removes one heart (2 HP) of maximum health. */
public class FadingEffect extends MobEffect {
    public FadingEffect() {
        super(MobEffectCategory.HARMFUL, 0x4B2A7A);
        this.addAttributeModifier(Attributes.MAX_HEALTH,
                ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "effect.fading"),
                -2.0D, AttributeModifier.Operation.ADD_VALUE);
    }
}
