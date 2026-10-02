package com.hollowexpanse.registry;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.effect.FadingEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    private ModEffects() {}

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HollowExpanse.MOD_ID);

    public static final RegistryObject<MobEffect> FADING = EFFECTS.register("fading", FadingEffect::new);

    /** MobEffectInstance needs a Holder in 1.21. */
    public static Holder<MobEffect> fading() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(FADING.get());
    }
}
