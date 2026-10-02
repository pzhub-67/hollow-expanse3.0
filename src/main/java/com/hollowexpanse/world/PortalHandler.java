package com.hollowexpanse.world;

import com.hollowexpanse.registry.ModBlocks;
import com.hollowexpanse.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** Stand inside the membrane for ~3 seconds to cross. */
public final class PortalHandler {
    private static final String TICKS = "he_portal_ticks";
    private static final String WAIT = "he_portal_wait";
    private static final String EXIT_DIM = "he_exit_dim";
    private static final String EXIT_POS = "he_exit_pos";
    private static final int CROSS_TICKS = 60;

    private PortalHandler() {}

    public static void tick(ServerPlayer player, ServerLevel level) {
        CompoundTag data = player.getPersistentData();
        BlockPos feet = player.blockPosition();
        boolean inside = level.getBlockState(feet).is(ModBlocks.HOLLOW_PORTAL.get())
                || level.getBlockState(feet.above()).is(ModBlocks.HOLLOW_PORTAL.get());
        if (!inside) {
            data.putBoolean(WAIT, false);
            int t = data.getInt(TICKS);
            if (t > 0) data.putInt(TICKS, Math.max(0, t - 2));
            return;
        }
        if (data.getBoolean(WAIT)) return;

        int ticks = data.getInt(TICKS) + 1;
        data.putInt(TICKS, ticks);
        if (ticks % 8 == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(),
                    10, 0.3D, 0.6D, 0.3D, 0.05D);
        }
        if (ticks >= (player.isCreative() ? 2 : CROSS_TICKS)) {
            data.putInt(TICKS, 0);
            data.putBoolean(WAIT, true);
            if (level.dimension() == ModDimensions.HOLLOW_EXPANSE) {
                goHome(player);
            } else {
                enterHollow(player, level);
            }
        }
    }

    private static void enterHollow(ServerPlayer player, ServerLevel from) {
        ServerLevel hollow = player.server.getLevel(ModDimensions.HOLLOW_EXPANSE);
        if (hollow == null) {
            player.displayClientMessage(Component.translatable("message.hollow_expanse.dimension_missing"), true);
            return;
        }
        CompoundTag data = player.getPersistentData();
        if (!ModDimensions.isHollow(from)) {
            data.putString(EXIT_DIM, from.dimension().location().toString());
            data.putLong(EXIT_POS, player.blockPosition().asLong());
        }
        BlockPos stand = Travel.buildHub(hollow, player.getBlockX(), player.getBlockZ());
        Travel.teleport(player, hollow, stand);
        hollow.playSound(null, stand, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.3F, 1.4F);
    }

    private static void goHome(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        ServerLevel dest = null;
        BlockPos pos = null;
        if (data.contains(EXIT_DIM)) {
            ResourceLocation id = ResourceLocation.tryParse(data.getString(EXIT_DIM));
            if (id != null) {
                dest = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
                pos = BlockPos.of(data.getLong(EXIT_POS));
            }
        }
        if (dest == null || ModDimensions.isHollow(dest)) {
            dest = player.server.getLevel(Level.OVERWORLD);
            if (dest == null) return;
            pos = dest.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, dest.getSharedSpawnPos());
        }
        Travel.teleport(player, dest, pos);
        dest.playSound(null, pos, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.3F, 1.1F);
    }
}
