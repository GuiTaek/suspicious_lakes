package com.gmail.guitaekm.suspicious_lakes.mixin;

/// suggested by ChatGPT in case ender pearl breaks some of its cautious pearl broken tests
/// not necessary for 1.21 but because ender pearls did change in later versions a lot will keep it

import com.gmail.guitaekm.suspicious_lakes.SuspiciousLakes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {

    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "onCollision", cancellable = true)
    private void onCollisionHead(HitResult hitResult, CallbackInfo ci) {
        if (!(hitResult instanceof BlockHitResult blockHit)) {
            return;
        }
        World world = this.getWorld();
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!SuspiciousLakes.SUSPICIOUS_LIQUID_BLOCK.shouldTeleport(this, state, pos)) {
            return;
        }
        boolean success = SuspiciousLakes.SUSPICIOUS_LIQUID_BLOCK.teleport(this.getWorld(), this, pos);
        if (!success) {
            return;
        }
        // ChatGPT suggested it in order to be future-compatible
        ci.cancel();
    }
}
