package com.hollowexpanse.world;

import com.hollowexpanse.block.HollowPortalBlock;
import com.hollowexpanse.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Safe landings, spawn platforms and the arrival hub with its return portal. */
public final class Travel {
    private Travel() {}

    public static void teleport(ServerPlayer player, ServerLevel level, BlockPos stand) {
        player.fallDistance = 0.0F;
        player.teleportTo(level, stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D,
                player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
    }

    /** Highest standable spot in the column between fromY and toY, or null. */
    public static BlockPos findStand(ServerLevel level, int x, int z, int fromY, int toY) {
        for (int y = fromY; y >= toY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty()
                    && level.getBlockState(pos.above()).isAir()
                    && level.getBlockState(pos.above(2)).isAir()) {
                return pos.above();
            }
        }
        return null;
    }

    /** Small glowing platform with a Lumen Bloom, for falling players with nothing to land on. */
    public static BlockPos safeSpot(ServerLevel level, int x, int z, int fromY, int toY, int platformY) {
        BlockPos found = findStand(level, x, z, fromY, toY);
        if (found != null) return found;
        BlockPos stand = new BlockPos(x, platformY, z);
        platform(level, stand, 2);
        level.setBlock(stand.offset(2, 0, 2), ModBlocks.LUMEN_BLOOM.get().defaultBlockState(), 3);
        return stand;
    }

    private static void platform(ServerLevel level, BlockPos stand, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                boolean edge = Math.abs(dx) == radius || Math.abs(dz) == radius;
                level.setBlock(stand.offset(dx, -1, dz),
                        (edge ? ModBlocks.HOLLOW_STONE : ModBlocks.VOIDMOSS).get().defaultBlockState(), 3);
                for (int dy = 0; dy <= 3; dy++) {
                    level.setBlock(stand.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    /** Arrival hub for the portal: a 9x9 platform, an Anchor Stone and a lit return portal. */
    public static BlockPos buildHub(ServerLevel level, int x, int z) {
        BlockPos stand = findStand(level, x, z, 100, 40);
        if (stand == null) stand = new BlockPos(x, 90, z);
        int floorY = stand.getY() - 1;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                boolean rim = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                level.setBlock(new BlockPos(x + dx, floorY, z + dz),
                        (rim ? ModBlocks.HOLLOW_STONE : ModBlocks.VOIDMOSS).get().defaultBlockState(), 2);
                for (int dy = 1; dy <= 7; dy++) {
                    level.setBlock(new BlockPos(x + dx, floorY + dy, z + dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        // portal frame (axis X) three blocks north of the landing spot
        int pz = z - 3;
        BlockState frame = ModBlocks.REINFORCED_OBSIDIAN.get().defaultBlockState();
        BlockState portal = ModBlocks.HOLLOW_PORTAL.get().defaultBlockState()
                .setValue(HollowPortalBlock.AXIS, Direction.Axis.X);
        for (int fx = x - 2; fx <= x + 1; fx++) {
            for (int fy = floorY; fy <= floorY + 4; fy++) {
                boolean border = fx == x - 2 || fx == x + 1 || fy == floorY || fy == floorY + 4;
                level.setBlock(new BlockPos(fx, fy, pz), border ? frame : portal, 2);
            }
        }
        level.setBlock(new BlockPos(x + 3, floorY + 1, z + 1), ModBlocks.ANCHOR_STONE.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x - 3, floorY + 1, z + 2), ModBlocks.LUMEN_BLOOM.get().defaultBlockState(), 3);
        level.setBlock(new BlockPos(x + 3, floorY + 1, z + 3), ModBlocks.LUMEN_BLOOM.get().defaultBlockState(), 3);
        return new BlockPos(x, floorY + 1, z);
    }
}
