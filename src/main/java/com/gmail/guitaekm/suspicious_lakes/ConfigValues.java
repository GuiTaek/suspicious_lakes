package com.gmail.guitaekm.suspicious_lakes;

import java.util.List;

public class ConfigValues {
    public static final int nrLakes = 42_445_223;
    public static final double powerDistance = 1.1;
    public static final List<Integer> cycleWeights = List.of(
            0, 5, 5, 3, 3, 1, 1
    );
    public static final int[] factsPhi = new int[] { 2, 21222611 };

    public static final double minimumDistance = 12;
    public static final double alpha = 0.5;

    //better don't touch this value, it's so difficult to balance so the game doesn't crash immediately
    public static final double lambda = 0.01;
    public static final int lastUnsafeChunkCoord = 64;
}
