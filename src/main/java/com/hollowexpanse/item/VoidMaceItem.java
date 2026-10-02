package com.hollowexpanse.item;

import com.hollowexpanse.HollowExpanse;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.AABB;

/** A heavy mace: hit while falling to slam for bonus damage and a shockwave. */
public class VoidMaceItem extends Item {
    public VoidMaceItem(Properties properties) {
        super(properties);
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.4D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        float fall = attacker.fallDistance;
        if (fall > 1.5F && !attacker.onGround() && attacker.level() instanceof ServerLevel level) {
            float capped = Math.min(fall, 14.0F);
            target.hurt(attacker instanceof Player p
                    ? level.damageSources().playerAttack(p)
                    : level.damageSources().mobAttack(attacker), capped * 1.5F);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(target.blockPosition()).inflate(3.5D),
                    e -> e != attacker && e != target && e.isAlive());
            for (LivingEntity e : nearby) {
                e.hurt(level.damageSources().magic(), capped * 0.5F);
                e.setDeltaMovement(e.getDeltaMovement().add(0.0D, 0.45D, 0.0D));
                e.hurtMarked = true;
            }
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getY() + 0.3D, target.getZ(),
                    50, 1.2D, 0.2D, 1.2D, 0.1D);
            level.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0D, target.getZ(),
                    1, 0, 0, 0, 0);
            level.playSound(null, target.blockPosition(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 1.2F, 0.7F);
            attacker.resetFallDistance();
        }
        return true;
    }
}
