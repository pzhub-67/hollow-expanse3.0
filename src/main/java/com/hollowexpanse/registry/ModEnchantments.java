package com.hollowexpanse.registry;

import com.hollowexpanse.HollowExpanse;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * In 1.21.1 enchantments are data-driven (data/hollow_expanse/enchantment/*.json).
 * This class only holds their keys and level lookups; behaviour lives in ForgeEvents.
 */
public final class ModEnchantments {
    private ModEnchantments() {}

    public static final ResourceKey<Enchantment> GRAVITY_ANCHOR = key("gravity_anchor");
    public static final ResourceKey<Enchantment> VOID_SIPHON = key("void_siphon");
    public static final ResourceKey<Enchantment> ECHO_STRIKE = key("echo_strike");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, name));
    }

    public static int itemLevel(Level level, ResourceKey<Enchantment> key, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    /** Gravity Anchor only counts on the chestplate and boots. */
    public static int gravityAnchorLevel(LivingEntity entity) {
        return itemLevel(entity.level(), GRAVITY_ANCHOR, entity.getItemBySlot(EquipmentSlot.CHEST))
                + itemLevel(entity.level(), GRAVITY_ANCHOR, entity.getItemBySlot(EquipmentSlot.FEET));
    }

    /** 0.0 (none) .. 0.9 (maximum) resistance to knockback and gravity pulls. Sneaking doubles it. */
    public static double gravityResistance(LivingEntity entity) {
        int lvl = gravityAnchorLevel(entity);
        if (lvl <= 0) return 0.0D;
        double base = lvl * 0.08D;
        return Math.min(0.9D, entity.isShiftKeyDown() ? base * 2.2D : base);
    }
}
