package com.gmail.guitaekm.enderlakes;

import java.util.ArrayList;
import java.util.List;

public class ConfigInstance {

    final private int nrLakes;
    final private double powerDistance;
    final private List<Integer> cycleWeights;
    final private int minimumDistance;
    final private int[] factsPhi;
    final private boolean autoUpdate;
    final private int lastUnsafeChunk;

    public ConfigInstance() {
        this(
                ConfigValues.nrLakes,
                ConfigValues.powerDistance,
                ConfigValues.cycleWeights,
                ConfigValues.minimumDistance,
                ConfigValues.factsPhi,
                ConfigValues.lastUnsafeChunkCoord,
                // todo: change to true, when the dependencies are inverted
                // it can't be changed now because else the test needs to load Enderlakes which crashes the tests
                false
        );
    }
    public ConfigInstance(
            int nrLakes,
            double powerDistance,
            List<Integer> cycleWeights,
            int minimumDistance,
            int[] factsPhi,
            int lastUnsafeChunk
    ) {
        this(nrLakes, powerDistance, cycleWeights, minimumDistance, factsPhi, lastUnsafeChunk, false);
    }
    private ConfigInstance (
            int nrLakes,
            double powerDistance,
            List<Integer> cycleWeights,
            int minimumDistance,
            int[] factsPhi,
            int lastUnsafeChunk,
            boolean autoUpdate
    ) {
        this.nrLakes = nrLakes;
        this.powerDistance = powerDistance;
        this.cycleWeights = new ArrayList<>(cycleWeights);
        this.minimumDistance = minimumDistance;
        this.factsPhi = factsPhi;
        this.lastUnsafeChunk = lastUnsafeChunk;
        this.autoUpdate = autoUpdate;
    }

    public ConfigInstance autoupdated() {
        return new ConfigInstance(
                this.nrLakes,
                this.powerDistance,
                this.cycleWeights,
                this.minimumDistance,
                this.factsPhi,
                this.lastUnsafeChunk,
                true
        );
    }

    public int nrLakes() {
        if (this.autoUpdate) {
            return WorldBorderConfigUpdater.INSTANCE.nrLakes();
        }
        return this.nrLakes;
    }
    public double powerDistance() {
        return this.powerDistance;
    }
    public List<Integer> cycleWeights() {
        return new ArrayList<>(this.cycleWeights);
    }
    public int minimumDistance() {
        return this.minimumDistance;
    }
    public int[] factsPhi() {
        if (this.autoUpdate) {
            return WorldBorderConfigUpdater.INSTANCE.factsPhi().clone();
        }
        return this.factsPhi.clone();
    }
    public int lastUnsafeChunk() {
        return this.lastUnsafeChunk;
    }
}
