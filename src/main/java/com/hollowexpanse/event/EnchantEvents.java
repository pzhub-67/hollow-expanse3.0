package com.hollowexpanse.event;

import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.registry.ModDimensions;
import com.hollowexpanse.registry.ModEnchantments;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Behaviour of Gravity Anchor, Void Siphon and Echo Strike (their stats live in data/.../enchantment). */
@Mod.EventBusSubscriber(modid = HollowExpanse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EnchantEvents {
    private static final List<PendingEcho> ECHOES = new ArrayList<>();
    private static boolean inEcho = false;

    private EnchantEvents() {}

    private record PendingEcho(ServerLevel level, UUID attacker, UUID target, float damage, long due) {}

    /** Gravity Anchor: resist knockback (doubled while sneaking). */
    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        double resist = ModEnchantments.gravityResistance(event.getEntity());
        if (resist > 0.0D) {
            event.setStrength((float) (event.getStrength() * (1.0D - resist)));
        }
    }

    /** Void Siphon: kills restore hunger and health, doubled inside the Hollow dimensions. */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        int level = ModEnchantments.itemLevel(player.level(), ModEnchantments.VOID_SIPHON, player.getMainHandItem());
        if (level <= 0) return;
        float mult = ModDimensions.isHollow(player.level()) ? 2.0F : 1.0F;
        player.getFoodData().eat((int) (level * 2 * mult), 0.3F);
        player.heal(level * mult);
        if (player.level() instanceof ServerLevel serverLevel) {
            LivingEntity victim = event.getEntity();
            serverLevel.sendParticles(ParticleTypes.SOUL, victim.getX(), victim.getY() + 1.0D, victim.getZ(),
                    8, 0.3D, 0.5D, 0.3D, 0.04D);
        }
    }

    /** Echo Strike: 30% chance that a melee hit repeats one second later as a ghost attack. */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (inEcho) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().getDirectEntity() != player) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        int enchant = ModEnchantments.itemLevel(level, ModEnchantments.ECHO_STRIKE, player.getMainHandItem());
        if (enchant <= 0 || player.getRandom().nextFloat() > 0.30F) return;
        ECHOES.add(new PendingEcho(level, player.getUUID(), event.getEntity().getUUID(),
                event.getAmount() * 0.6F, level.getGameTime() + 20L));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ECHOES.isEmpty()) return;
        Iterator<PendingEcho> it = ECHOES.iterator();
        while (it.hasNext()) {
            PendingEcho echo = it.next();
            if (echo.level().getGameTime() < echo.due()) continue;
            it.remove();
            Entity target = echo.level().getEntity(echo.target());
            if (!(target instanceof LivingEntity living) || !living.isAlive()) continue;
            Player attacker = echo.level().getPlayerByUUID(echo.attacker());
            inEcho = true;
            try {
                living.hurt(attacker != null
                        ? echo.level().damageSources().playerAttack(attacker)
                        : echo.level().damageSources().magic(), echo.damage());
            } finally {
                inEcho = false;
            }
            echo.level().sendParticles(ParticleTypes.SCULK_SOUL, living.getX(), living.getY() + 1.0D, living.getZ(),
                    10, 0.3D, 0.5D, 0.3D, 0.03D);
            echo.level().playSound(null, living.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.PLAYERS, 1.0F, 1.6F);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ECHOES.clear();
    }
}
