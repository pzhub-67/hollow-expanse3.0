package com.hollowexpanse.item;

import com.hollowexpanse.world.ModAdvancements;
import com.hollowexpanse.world.TetherAnchors;
import com.hollowexpanse.world.Travel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Right-click: reel yourself toward the block you are looking at.
 * Sneak + right-click: recall to the Anchor Stone you last bound (same dimension).
 */
public class TetherHookItem extends Item {
    private static final String USES = "he_tether_uses";
    private final double range;
    private final double power;

    public TetherHookItem(double range, double power, Properties properties) {
        super(properties);
        this.range = range;
        this.power = power;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.isShiftKeyDown()) {
            return recall(level, player, hand, stack);
        }
        HitResult hit = player.pick(range, 1.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.hollow_expanse.tether_no_target"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            Vec3 from = player.getEyePosition();
            Vec3 target = hit.getLocation();
            Vec3 line = target.subtract(from);
            double dist = line.length();
            Vec3 pull = line.normalize().scale(Math.min(power, 0.4D + dist * 0.07D));
            player.setDeltaMovement(pull.x, pull.y + 0.22D, pull.z);
            player.hurtMarked = true;
            player.resetFallDistance();
            if (level instanceof ServerLevel serverLevel) {
                int steps = (int) Math.min(40, dist * 2);
                for (int i = 0; i <= steps; i++) {
                    Vec3 p = from.add(line.scale(i / (double) Math.max(1, steps)));
                    serverLevel.sendParticles(ParticleTypes.END_ROD, p.x, p.y - 0.2D, p.z, 1, 0, 0, 0, 0);
                }
            }
            level.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE,
                    SoundSource.PLAYERS, 1.0F, 1.5F);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            player.getCooldowns().addCooldown(this, 12);
            countUse(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private InteractionResultHolder<ItemStack> recall(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        var anchor = TetherAnchors.get(sp);
        if (anchor.isEmpty() || anchor.get().dimension() != level.dimension()) {
            sp.displayClientMessage(Component.translatable("message.hollow_expanse.no_anchor"), true);
            return InteractionResultHolder.fail(stack);
        }
        Travel.teleport(sp, serverLevel, anchor.get().pos().above());
        serverLevel.playSound(null, anchor.get().pos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.6F);
        stack.hurtAndBreak(6, player, LivingEntity.getSlotForHand(hand));
        player.getCooldowns().addCooldown(this, 400);
        return InteractionResultHolder.success(stack);
    }

    private static void countUse(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;
        int uses = sp.getPersistentData().getInt(USES) + 1;
        sp.getPersistentData().putInt(USES, uses);
        if (uses == 50) {
            ModAdvancements.award(sp, "tether_master", "use");
        }
    }
}
