package com.hollowexpanse.block;

import com.hollowexpanse.world.TetherAnchors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Right-click to bind yourself: sets your recall point and respawn point on this stone. */
public class AnchorStoneBlock extends Block {
    public AnchorStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                            Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            TetherAnchors.bind(serverPlayer, pos);
            serverPlayer.setRespawnPosition(level.dimension(), pos.above(), 0.0F, true, false);
            serverPlayer.displayClientMessage(Component.translatable("message.hollow_expanse.anchor_bound"), true);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.4F, 0.7F);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D, 24, 0.4D, 0.5D, 0.4D, 0.02D);
        }
        return InteractionResult.CONSUME;
    }
}
