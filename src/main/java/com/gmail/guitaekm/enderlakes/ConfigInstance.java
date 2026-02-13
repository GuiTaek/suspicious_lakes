package com.gmail.guitaekm.enderlakes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ConfigInstance {

    final private double powerDistance;
    final private List<Integer> cycleWeights;
    final private int minimumDistance;
    final private int lastUnsafeChunk;

    private int nrLakes;
    private int[] factsPhi;
    private boolean sourceChanged;
    private NrLakesSource nrLakesSource;

    public enum NrLakesSourceType {
        RAW,
        BORDER
    }

    public record NrLakesSource(NrLakesSourceType type, int val) { }

    public static NrLakesSource borderSource(int border) {
        return new NrLakesSource(NrLakesSourceType.BORDER, border);
    }

    public static NrLakesSource rawSource(int nrLakes) {
        assert BigInteger.valueOf(nrLakes).isProbablePrime(1_000);
        return new NrLakesSource(NrLakesSourceType.RAW, nrLakes);
    }

    public ConfigInstance() {
        this(
                rawSource(ConfigValues.nrLakes),
                ConfigValues.powerDistance,
                ConfigValues.cycleWeights,
                ConfigValues.minimumDistance,
                ConfigValues.lastUnsafeChunkCoord
        );
    }
    public ConfigInstance (
            NrLakesSource nrLakesSource,
            double powerDistance,
            List<Integer> cycleWeights,
            int minimumDistance,
            int lastUnsafeChunk
    ) {
        this.powerDistance = powerDistance;
        this.cycleWeights = new ArrayList<>(cycleWeights);
        this.minimumDistance = minimumDistance;
        this.lastUnsafeChunk = lastUnsafeChunk;

        this.nrLakesSource = nrLakesSource;
        this.sourceChanged = true;
        this.updateNrLakesAndFactsPhi();
    }

    private void updateNrLakesAndFactsPhi() {
        if (!this.sourceChanged) {
            return;
        }
        switch (this.nrLakesSource.type) {
            case RAW -> {
                this.nrLakes = this.nrLakesSource.val;
            }
            case BORDER -> {
                LakeDestinationFinder finder = new LakeDestinationFinder(this);
                this.nrLakes = finder.findNewNrLakes(this.nrLakesSource.val, -1);
            }
        }
        this.factsPhi = LakeDestinationFinder
                .primeFactors(this.nrLakes - 1)
                .stream()
                .mapToInt(elem -> elem)
                .toArray();
        this.sourceChanged = false;
    }

    public void setNrLakesSource(NrLakesSource source) {
        if (this.nrLakesSource.type == source.type) {
            if (this.nrLakesSource.val == source.val) {
                return;
            }
        }
        this.nrLakesSource = source;
        this.sourceChanged = true;
    }

    public int nrLakes() {
        this.updateNrLakesAndFactsPhi();
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
        this.updateNrLakesAndFactsPhi();
        return this.factsPhi.clone();
    }
    public int lastUnsafeChunk() {
        return this.lastUnsafeChunk;
    }
}
