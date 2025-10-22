package com.example.villageroads;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class RoadGenerator {
    
    public static void generateRoad(ServerWorld world, BlockPos start, BlockPos end) {
        int x1 = start.getX();
        int z1 = start.getZ();
        int x2 = end.getX();
        int z2 = end.getZ();
        
        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;
        
        int x = x1;
        int z = z1;
        int step = 0;
        
        while (step < 10000) {
            placeRoadSection(world, x, z);
            
            if (x == x2 && z == z2) break;
            
            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                z += sz;
            }
            step++;
        }
    }
    
    private static void placeRoadSection(ServerWorld world, int x, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = new BlockPos(x + dx, 0, z + dz);
                int y = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
                
                BlockPos groundPos = new BlockPos(pos.getX(), y - 1, pos.getZ());
                BlockPos airPos = new BlockPos(pos.getX(), y, pos.getZ());
                
                try {
                    BlockState currentBlock = world.getBlockState(groundPos);
                    if (!currentBlock.isOf(Blocks.WATER) && 
                        !currentBlock.isOf(Blocks.LAVA) &&
                        !currentBlock.isOf(Blocks.BEDROCK) &&
                        !currentBlock.isAir()) {
                        
                        world.setBlockState(groundPos, Blocks.STONE_BRICKS.getDefaultState(), 3);
                        
                        BlockState aboveBlock = world.getBlockState(airPos);
                        if (aboveBlock.isOf(Blocks.GRASS) || 
                            aboveBlock.isOf(Blocks.TALL_GRASS) ||
                            aboveBlock.isOf(Blocks.FERN) ||
                            aboveBlock.isOf(Blocks.DANDELION) ||
                            aboveBlock.isOf(Blocks.POPPY)) {
                            world.setBlockState(airPos, Blocks.AIR.getDefaultState(), 3);
                        }
                    }
                } catch (Exception e) {
                    // Skip problematic blocks
                }
            }
        }
    }
}
