package com.hollowexpanse.item;

import com.hollowexpanse.entity.HollowWardenEntity;
import com.hollowexpanse.registry.ModDimensions;
import com.hollowexpanse.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;

/** Use on the ground in the Hollow Expanse to awaken the Hollow Warden. */
public class HollowSigilItem extends Item {
    public HollowSigilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (level.dimension() != ModDimensions.HOLLOW_EXPANSE) {
            if (player != null) player.displayClientMessage(Component.translatable("message.hollow_expanse.sigil_wrong_place"), true);
            return InteractionResult.FAIL;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getEntitiesOfClass(HollowWardenEntity.class, new AABB(pos).inflate(96.0D)).isEmpty()) {
            if (player != null) player.displayClientMessage(Component.translatable("message.hollow_expanse.sigil_already"), true);
            return InteractionResult.FAIL;
        }
        HollowWardenEntity warden = ModEntities.HOLLOW_WARDEN.get().spawn(level, pos, MobSpawnType.MOB_SUMMONED);
        if (warden == null) {
            return InteractionResult.FAIL;
        }
        warden.setHome(pos);
        level.playSound(null, pos, SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 3.0F, 0.6F);
        if (player != null && !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
