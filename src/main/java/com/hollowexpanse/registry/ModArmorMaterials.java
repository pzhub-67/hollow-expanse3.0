package com.hollowexpanse.registry;

import com.hollowexpanse.HollowExpanse;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public final class ModArmorMaterials {
    private ModArmorMaterials() {}

    /**
     * Held directly (not via a registry) so armor items can be constructed at any point
     * of the registration order. Texture layers: textures/models/armor/voidsteel_layer_1/2.png
     */
    public static final Holder<ArmorMaterial> VOIDSTEEL = Holder.direct(new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET, 3,
                   ArmorItem.Type.CHESTPLATE, 8,
                   ArmorItem.Type.LEGGINGS, 6,
                   ArmorItem.Type.BOOTS, 3,
                   ArmorItem.Type.BODY, 11),
            18,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(ModItems.VOIDSTEEL_INGOT.get()),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, "voidsteel"))),
            2.5F,
            0.05F));
}
