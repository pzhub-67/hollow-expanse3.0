package com.hollowexpanse.entity;

import com.hollowexpanse.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A glowing crystal sliver fired by the Hollow Warden. Flies straight. */
public class VoidShardEntity extends ThrowableItemProjectile {
    public VoidShardEntity(EntityType<? extends VoidShardEntity> type, Level level) {
        super(type, level);
    }

    public VoidShardEntity(EntityType<? extends VoidShardEntity> type, LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.VOID_SHARD.get();
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        } else if (tickCount > 100) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        if (!level().isClientSide && hit instanceof LivingEntity living && hit != getOwner()) {
            living.hurt(level().damageSources().indirectMagic(this, getOwner()), 6.0F);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            discard();
        }
    }
}
