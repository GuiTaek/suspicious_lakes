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

    public void register() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.END) {
                this.borderFinished = LocalDateTime.now();
                WorldBorder border = world.getWorldBorder();
                border.addListener(this);
                Enderlakes.finder.config.setNrLakesSource(ConfigInstance.borderSource((int)border.getSize()));
            }
        });
    }

    public boolean mayUseLakes() {
        return LocalDateTime.now().isAfter(borderFinished);
    }

    @Override
    public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time) {
        this.borderFinished = LocalDateTime.now()
                .plusSeconds(time / 1_000)
                .plusNanos((time % 1_000) * 1_000_000);

        Enderlakes.finder.config.setNrLakesSource(ConfigInstance.borderSource((int) toSize));
    }

    @Override
    public void onSizeChange(WorldBorder border, double size) {
        Enderlakes.finder.config.setNrLakesSource(ConfigInstance.borderSource((int)size));
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
