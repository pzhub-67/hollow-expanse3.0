package com.hollowexpanse.world;

import com.hollowexpanse.HollowExpanse;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancements {
    private ModAdvancements() {}

    public static void award(ServerPlayer player, String path, String criterion) {
        AdvancementHolder holder = player.server.getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(HollowExpanse.MOD_ID, path));
        if (holder != null) {
            player.getAdvancements().award(holder, criterion);
        }
    }
}
