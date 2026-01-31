package com.gmail.guitaekm.enderlakes;

import java.util.List;

public record ConfigInstance (
        int nrLakes,
        double powerDistance,
        List<Integer> cycleWeights,
        int minimumDistance,
        int[] factsPhi
) {
    public ConfigInstance() {
        this(
                new ConfigValues().nrLakes,
                new ConfigValues().powerDistance,
                new ConfigValues().cycleWeights,
                new ConfigValues().minimumDistance,
                new ConfigValues().factsPhi
        );
    }
}
