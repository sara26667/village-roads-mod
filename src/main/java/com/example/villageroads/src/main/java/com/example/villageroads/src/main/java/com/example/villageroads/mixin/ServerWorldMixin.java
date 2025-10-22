package com.example.villageroads.mixin;

import com.example.villageroads.VillageRoads;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    
    private int tickCounter = 0;
    private static final int CHECK_INTERVAL = 600;
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        tickCounter++;
        if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            ServerWorld world = (ServerWorld) (Object) this;
            
            if (!world.getPlayers().isEmpty()) {
                BlockPos playerPos = world.getPlayers().get(0).getBlockPos();
                VillageRoads.generateRoadsBetweenVillages(world, playerPos);
            }
        }
    }
}
