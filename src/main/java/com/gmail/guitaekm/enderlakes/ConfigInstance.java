package com.gmail.guitaekm.enderlakes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigInstance {

    final private double powerDistance;
    final private List<Integer> cycleWeights;
    final private int minimumDistance;
    final private int lastUnsafeChunk;

    private long seed;
    private Long oldSeed = null;
    private int g;
    private int gInv;
    private int nrLakes;
    private int[] factsPhi;
    private boolean sourceChanged;
    private NrLakesSource nrLakesSource;
    private Integer oldNrLakes = null;

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
    public void setSeed(long seed) {
        this.seed = seed;
    }
    public void updateG() {
        if (!Objects.equals(this.oldSeed, this.seed) || !Objects.equals(this.oldNrLakes, this.nrLakes())) {
            this.g = LakeDestinationFinder.calculateG(this.nrLakes(), this.factsPhi(), this.seed);
            this.gInv = LakeDestinationFinder.calculateInv(this.nrLakes(), this.g);
            this.oldSeed = this.seed;
            this.oldNrLakes = this.nrLakes();
        }
    }
    public int g() {
        this.updateG();
        return this.g;
    }
    public int gInv() {
        this.updateG();
        return this.gInv;
    }
}
