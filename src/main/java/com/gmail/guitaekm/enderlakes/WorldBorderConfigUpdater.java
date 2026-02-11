package com.gmail.guitaekm.enderlakes;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;

import java.time.LocalDateTime;

public class WorldBorderConfigUpdater implements WorldBorderListener {

    private WorldBorderConfigUpdater() { }

    public static WorldBorderConfigUpdater INSTANCE = new WorldBorderConfigUpdater();
    private LocalDateTime borderFinished;
    private double size = -1;
    private double oldSize;
    private int nrLakes;
    private int[] factsPhi;

    public void register() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.END) {
                this.borderFinished = LocalDateTime.now();
                WorldBorder border = world.getWorldBorder();
                border.addListener(this);
                this.size = border.getSize();
                this.updateBorderSize();
            }
        });
    }

    private void updateBorderSize() {
        if (this.oldSize == this.size) {
            return;
        }
        this.nrLakes = LakeDestinationFinder.findNewNrLakes(Enderlakes.CONFIG, (int) this.size, -1);
        this.factsPhi = LakeDestinationFinder
                .primeFactors(this.nrLakes - 1)
                .stream()
                .mapToInt(elem -> elem)
                .toArray();
        this.oldSize = this.size;
    }

    public boolean mayUseLakes() {
        return LocalDateTime.now().isAfter(borderFinished);
    }

    public int nrLakes() {
        this.updateBorderSize();
        return this.nrLakes;
    }

    public int[] factsPhi() {
        this.updateBorderSize();

        // cloning so no calling code can change it
        return this.factsPhi.clone();
    }

    @Override
    public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
        this.borderFinished = LocalDateTime.now()
                .plusSeconds(time / 1_000)
                .plusNanos((time % 1_000) * 1_000_000);

        // calculate the config adjustments lazy so a change border command called every tick wouldn't crash the game
        this.size = toSize;
    }

    @Override
    public void onSizeChange(WorldBorder border, double size) {
        // doesn't seem to be fired
    }

    @Override
    public void onCenterChanged(WorldBorder border, double centerX, double centerZ) { }

    @Override
    public void onWarningTimeChanged(WorldBorder border, int warningTime) { }

    @Override
    public void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance) { }

    @Override
    public void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock) { }

    @Override
    public void onSafeZoneChanged(WorldBorder border, double safeZoneRadius) { }
}
