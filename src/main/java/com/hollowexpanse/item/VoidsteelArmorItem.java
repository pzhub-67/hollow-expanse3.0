package com.hollowexpanse.item;

import com.hollowexpanse.registry.ModArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;

public class VoidsteelArmorItem extends ArmorItem {
    public VoidsteelArmorItem(Type type, Properties properties) {
        super(ModArmorMaterials.VOIDSTEEL, type, properties.durability(type.getDurability(38)));
    }

    /** Weightless Step: all four pieces worn. */
    public static boolean hasFullSet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof VoidsteelArmorItem
                && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof VoidsteelArmorItem
                && entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof VoidsteelArmorItem
                && entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof VoidsteelArmorItem;
    }
}
