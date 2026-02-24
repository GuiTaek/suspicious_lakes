package com.gmail.guitaekm.suspicious_lakes.mixin;

import com.gmail.guitaekm.suspicious_lakes.EntityLakeTeleportScheduler;
import com.gmail.guitaekm.suspicious_lakes.SuspiciousLakes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements EntityLakeTeleportScheduler {
    @Shadow
    private World world;
    @Unique
    private BlockPos requestedLakeTeleport;

    @Inject(at = @At("TAIL"), method="<init>")
    public void constructorTail(EntityType<?> type, World world, CallbackInfo ci) {
        requestedLakeTeleport = null;
    }

    @Inject(at = @At("TAIL"), method="tick")
    public void tickTail(CallbackInfo ci) {
        if (!(this.world instanceof ServerWorld serverWorld)) {
            return;
        }
        if (this.requestedLakeTeleport == null) {
            return;
        }
        BlockState state = this.world.getBlockState(requestedLakeTeleport);
        SuspiciousLakes.SUSPICIOUS_LIQUID_BLOCK.testTeleport(state, serverWorld, requestedLakeTeleport, (Entity)(Object)this);
        this.requestedLakeTeleport = null;
    }

    @Override
    public void suspiciousLakes$setRequestingBlock(BlockPos pos) {
        this.requestedLakeTeleport = new BlockPos(pos);
    }
}
