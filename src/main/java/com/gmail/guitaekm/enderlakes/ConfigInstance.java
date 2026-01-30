package com.gmail.guitaekm.enderlakes;

import java.util.List;

public record ConfigInstance (
        int nrLakes,
        double powerDistance,
        List<Integer> cycleWeights,
        int minimumDistance
) {
    public ConfigInstance() {
        this(
                new ConfigModel().nrLakes,
                new ConfigModel().powerDistance,
                new ConfigModel().cycleWeights,
                new ConfigModel().minimumDistance
        );
    }
    public ConfigInstance(Config config) {
        this(
                config.nrLakes(),
                config.powerDistance(),
                config.cycleWeights(),
                config.minimumDistance()
        );
    }
}
