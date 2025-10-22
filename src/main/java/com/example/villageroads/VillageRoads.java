package com.example.villageroads;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.StructureTags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VillageRoads implements ModInitializer {
    public static final String MOD_ID = "villageroads";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final Map<ServerWorld, Set<String>> processedRoads = new HashMap<>();
    private static final int SEARCH_RADIUS = 100;

    @Override
    public void onInitialize() {
        LOGGER.info("Village Roads mod initialized!");
    }
    
    public static void generateRoadsBetweenVillages(ServerWorld world, BlockPos playerPos) {
        if (!processedRoads.containsKey(world)) {
            processedRoads.put(world, new HashSet<>());
        }
        
        Set<String> processed = processedRoads.get(world);
        List<BlockPos> villages = findNearbyVillages(world, playerPos);
        
        LOGGER.info("Found {} villages near player", villages.size());
        
        for (int i = 0; i < villages.size(); i++) {
            for (int j = i + 1; j < villages.size(); j++) {
                BlockPos v1 = villages.get(i);
                BlockPos v2 = villages.get(j);
                
                String roadKey = Math.min(v1.asLong(), v2.asLong()) + "-" + Math.max(v1.asLong(), v2.asLong());
                
                if (!processed.contains(roadKey)) {
                    double distance = Math.sqrt(v1.getSquaredDistance(v2));
                    if (distance < 800) {
                        RoadGenerator.generateRoad(world, v1, v2);
                        processed.add(roadKey);
                        LOGGER.info("Generated road: {} <-> {}", v1, v2);
                    }
                }
            }
        }
    }
    
    private static List<BlockPos> findNearbyVillages(ServerWorld world, BlockPos center) {
        List<BlockPos> villages = new ArrayList<>();
        ChunkPos centerChunk = new ChunkPos(center);
        
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x += 4) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z += 4) {
                ChunkPos chunkPos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                
                try {
                    Map<Structure, RegistryEntry<Structure>> structures = world.getStructureAccessor()
                        .getStructureReferences(chunkPos);
                    
                    for (RegistryEntry<Structure> entry : structures.values()) {
                        if (entry.isIn(StructureTags.VILLAGE)) {
                            BlockPos villagePos = new BlockPos(
                                chunkPos.getStartX() + 8,
                                world.getTopY() / 2,
                                chunkPos.getStartZ() + 8
                            );
                            villages.add(villagePos);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Skip problematic chunks
                }
            }
        }
        
        return villages;
    }
                  }
