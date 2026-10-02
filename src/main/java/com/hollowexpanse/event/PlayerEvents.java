package com.hollowexpanse.event;

import com.hollowexpanse.Config;
import com.hollowexpanse.HollowExpanse;
import com.hollowexpanse.item.VoidsteelArmorItem;
import com.hollowexpanse.registry.ModDimensions;
import com.hollowexpanse.registry.ModEffects;
import com.hollowexpanse.world.ModAdvancements;
import com.hollowexpanse.world.PortalHandler;
import com.hollowexpanse.world.Travel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Per-player server logic: portal, light/Fading, falling between layers, Weightless Step. */
@Mod.EventBusSubscriber(modid = HollowExpanse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerEvents {
    private static final int FALL_Y = -40;     // below the lowest island underside
    private static final int RIFT_Y = 300;     // top of the Underlayer climb

    private PlayerEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) return;

        PortalHandler.tick(player, level);

        // Weightless Step: full voidsteel set = slow fall (which also cancels fall damage).
        if (VoidsteelArmorItem.hasFullSet(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, false));
        }

        if (!ModDimensions.isHollow(level)) return;

        tickFading(player, level);

        if (level.dimension() == ModDimensions.HOLLOW_EXPANSE || level.dimension() == ModDimensions.UNDERLAYER) {
            if (player.getY() < FALL_Y && !player.isCreative() && !player.isSpectator()) {
                fallThrough(player, level);
            } else if (level.dimension() == ModDimensions.UNDERLAYER && player.getY() >= RIFT_Y) {
                riseThrough(player);
            }
        }
    }

    /** Light is life here: staying in the dark slowly thins you out (Fading = less max health). */
    private static void tickFading(ServerPlayer player, ServerLevel level) {
        if (!Config.fadingEnabled() || player.isCreative() || player.isSpectator()) return;
        CompoundTag data = player.getPersistentData();
        int dark = data.getInt("he_dark");
        int light = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
        if (light <= 2) {
            dark++;
        } else {
            dark = Math.max(0, dark - 4);
        }
        if (dark >= Config.fadingSeconds() * 20) {
            dark = 0;
            MobEffectInstance current = player.getEffect(ModEffects.fading());
            int amplifier = current == null ? 0 : Math.min(current.getAmplifier() + 1, 3);
            player.addEffect(new MobEffectInstance(ModEffects.fading(), 20 * 90, amplifier, false, true, true));
            player.displayClientMessage(Component.translatable("message.hollow_expanse.fading"), true);
        }
        data.putInt("he_dark", dark);
    }

    private static void fallThrough(ServerPlayer player, ServerLevel level) {
        if (!Config.fallToUnderlayer()) {
            return; // vanilla void damage takes over
        }
        ServerLevel under = player.server.getLevel(ModDimensions.UNDERLAYER);
        if (under == null) return;
        BlockPos stand = Travel.safeSpot(under, player.getBlockX(), player.getBlockZ(), 90, 20, 60);
        Travel.teleport(player, under, stand);
        under.playSound(null, stand, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.5F, 0.5F);
        player.displayClientMessage(Component.translatable("message.hollow_expanse.fell_into_underlayer"), true);
        ModAdvancements.award(player, "bottomless", "fall");
    }

    private static void riseThrough(ServerPlayer player) {
        ServerLevel hollow = player.server.getLevel(ModDimensions.HOLLOW_EXPANSE);
        if (hollow == null) return;
        BlockPos stand = Travel.safeSpot(hollow, player.getBlockX(), player.getBlockZ(), 175, 40, 100);
        Travel.teleport(player, hollow, stand);
        hollow.playSound(null, stand, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0F, 0.8F);
        player.displayClientMessage(Component.translatable("message.hollow_expanse.climbed_out"), true);
    }
}
