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

    public ConfigInstance() {
        this(
                new ConfigValues().nrLakes,
                new ConfigValues().powerDistance,
                new ConfigValues().cycleWeights,
                new ConfigValues().minimumDistance,
                new ConfigValues().factsPhi,
                false
        );
    }
    public ConfigInstance(
            int nrLakes,
            double powerDistance,
            List<Integer> cycleWeights,
            int minimumDistance,
            int[] factsPhi
    ) {
        this(nrLakes, powerDistance, cycleWeights, minimumDistance, factsPhi, false);
    }
    private ConfigInstance (
            int nrLakes,
            double powerDistance,
            List<Integer> cycleWeights,
            int minimumDistance,
            int[] factsPhi,
            boolean autoUpdate
    ) {
        this.nrLakes = nrLakes;
        this.powerDistance = powerDistance;
        this.cycleWeights = new ArrayList<>(cycleWeights);
        this.minimumDistance = minimumDistance;
        this.factsPhi = factsPhi;
        this.autoUpdate = autoUpdate;
    }

    public ConfigInstance autoupdated() {
        return new ConfigInstance(
                this.nrLakes,
                this.powerDistance,
                this.cycleWeights,
                this.minimumDistance,
                this.factsPhi,
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
}
