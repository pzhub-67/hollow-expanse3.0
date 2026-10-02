package com.hollowexpanse.world;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Stores each player's last bound Anchor Stone (dimension + position) on the player. */
public final class TetherAnchors {
    private static final String KEY = "he_anchor";

    private TetherAnchors() {}

    public record Anchor(ResourceKey<Level> dimension, BlockPos pos) {}

    public static void bind(ServerPlayer player, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", player.level().dimension().location().toString());
        tag.putLong("pos", pos.asLong());
        player.getPersistentData().put(KEY, tag);
    }

    public static Optional<Anchor> get(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) return Optional.empty();
        CompoundTag tag = root.getCompound(KEY);
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("dim"));
        if (id == null) return Optional.empty();
        return Optional.of(new Anchor(ResourceKey.create(Registries.DIMENSION, id), BlockPos.of(tag.getLong("pos"))));
    }
}
