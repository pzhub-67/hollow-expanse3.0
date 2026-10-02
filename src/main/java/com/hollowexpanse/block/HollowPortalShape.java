package com.hollowexpanse.block;

import com.hollowexpanse.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Finds a closed frame of Reinforced Obsidian (flood fill) and fills it with portal blocks. */
public final class HollowPortalShape {
    private static final int MAX_BLOCKS = 400;

    private HollowPortalShape() {}

    public static boolean tryLight(Level level, BlockPos frameBlock, Direction clickedFace) {
        BlockPos inside = frameBlock.relative(clickedFace);
        if (!level.getBlockState(inside).isAir()) {
            return false;
        }
        for (Direction.Axis axis : new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z}) {
            Set<BlockPos> interior = flood(level, inside, axis);
            if (interior != null) {
                BlockState portal = ModBlocks.HOLLOW_PORTAL.get().defaultBlockState()
                        .setValue(HollowPortalBlock.AXIS, axis);
                for (BlockPos pos : interior) {
                    level.setBlock(pos, portal, 2);
                }
                return true;
            }
        }
        return false;
    }

    /** @return interior positions of a valid frame in the plane spanned by `axis` and UP, or null. */
    public static Set<BlockPos> flood(Level level, BlockPos start, Direction.Axis axis) {
        Direction side = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction[] dirs = {side, side.getOpposite(), Direction.UP, Direction.DOWN};
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        int minY = start.getY(), maxY = start.getY();
        int minS = side == Direction.EAST ? start.getX() : start.getZ();
        int maxS = minS;
        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            for (Direction dir : dirs) {
                BlockPos next = cur.relative(dir);
                if (seen.contains(next)) continue;
                BlockState state = level.getBlockState(next);
                if (state.is(ModBlocks.REINFORCED_OBSIDIAN.get())) continue;
                if (!state.isAir()) return null;
                seen.add(next);
                if (seen.size() > MAX_BLOCKS) return null;
                queue.add(next);
                minY = Math.min(minY, next.getY());
                maxY = Math.max(maxY, next.getY());
                int s = side == Direction.EAST ? next.getX() : next.getZ();
                minS = Math.min(minS, s);
                maxS = Math.max(maxS, s);
            }
        }
        boolean bigEnough = (maxY - minY + 1) >= 3 && (maxS - minS + 1) >= 2;
        return bigEnough ? seen : null;
    }
}
