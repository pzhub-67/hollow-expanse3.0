package com.hollowexpanse.block;

import com.hollowexpanse.Config;
import com.hollowexpanse.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Frame block of the Hollow Portal. Light the frame with a Void Ignition Core. */
public class ReinforcedObsidianBlock extends Block {
    public ReinforcedObsidianBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                           Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.VOID_IGNITION_CORE.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (Config.portalOnlyInEnd() && level.dimension() != Level.END) {
            player.displayClientMessage(Component.translatable("message.hollow_expanse.portal_end_only"), true);
            return ItemInteractionResult.FAIL;
        }
        if (HollowPortalShape.tryLight(level, pos, hit.getDirection())) {
            level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8F, 1.3F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return ItemInteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable("message.hollow_expanse.portal_invalid"), true);
        return ItemInteractionResult.FAIL;
    }
}
