package com.hollowexpanse.item;

import com.hollowexpanse.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum VoidsteelTier implements Tier {
    /** Faster than diamond, durability just under netherite. */
    VOIDSTEEL(1900, 9.5F, 3.5F, 18, () -> Ingredient.of(ModItems.VOIDSTEEL_INGOT.get())),
    /** Voidsteel fused with netherite and a Warden's Core. */
    HOLLOWFORGED(2900, 11.0F, 4.5F, 22, () -> Ingredient.of(ModItems.WARDENS_CORE.get()));

    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantability;
    private final Supplier<Ingredient> repair;

    VoidsteelTier(int uses, float speed, float damage, int enchantability, Supplier<Ingredient> repair) {
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantability = enchantability;
        this.repair = repair;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return damage; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
    @Override public int getEnchantmentValue() { return enchantability; }
    @Override public Ingredient getRepairIngredient() { return repair.get(); }
}
